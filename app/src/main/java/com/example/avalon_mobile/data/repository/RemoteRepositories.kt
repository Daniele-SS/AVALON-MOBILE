package com.example.avalon_mobile.data.repository

import com.example.avalon_mobile.data.AppError
import com.example.avalon_mobile.data.AppResult
import com.example.avalon_mobile.data.local.LeituraLocal
import com.example.avalon_mobile.data.local.SessionManager
import com.example.avalon_mobile.data.map
import com.example.avalon_mobile.data.remote.ApiCall
import com.example.avalon_mobile.data.remote.AvalonApi
import com.example.avalon_mobile.data.remote.LeituraApi
import com.example.avalon_mobile.model.AlterarSenhaRequest
import com.example.avalon_mobile.model.SolicitarCodigoSenhaRequest
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.example.avalon_mobile.model.Avaliacao
import com.example.avalon_mobile.model.AvaliacaoPergunta
import com.example.avalon_mobile.model.BeneficioItem
import com.example.avalon_mobile.model.Colaborador
import com.example.avalon_mobile.model.Comunicado
import com.example.avalon_mobile.model.Dashboard
import com.example.avalon_mobile.model.DiaSemana
import com.example.avalon_mobile.model.Documento
import com.example.avalon_mobile.model.Feedback
import com.example.avalon_mobile.model.Ferias
import com.example.avalon_mobile.model.FeriasRequest
import com.example.avalon_mobile.model.JornadaEscala
import com.example.avalon_mobile.model.LoginRequest
import com.example.avalon_mobile.model.LogoutRequest
import com.example.avalon_mobile.model.Notificacao
import com.example.avalon_mobile.model.Pergunta
import com.example.avalon_mobile.model.Pesquisa
import com.example.avalon_mobile.model.RedefinirRecuperacaoRequest
import com.example.avalon_mobile.model.SolicitarRecuperacaoRequest
import com.example.avalon_mobile.model.RespostaAvaliacaoItem
import com.example.avalon_mobile.model.RespostaAvaliacaoLote
import com.example.avalon_mobile.model.RespostaConfirmacao
import com.example.avalon_mobile.model.RespostaFeedback
import com.example.avalon_mobile.model.RespostaFeedbackNova
import com.example.avalon_mobile.model.RespostaPesquisaItem
import com.example.avalon_mobile.model.RespostaPesquisaLote
import com.example.avalon_mobile.model.SolicitacaoCorrecao
import com.example.avalon_mobile.model.UsuarioResumo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class RemoteAuthRepository(
    private val api: AvalonApi,
    private val call: ApiCall,
    private val sessionManager: SessionManager,
    scope: CoroutineScope
) : AuthRepository {

    override val usuario: StateFlow<UsuarioResumo?> = sessionManager.session
        .map { it?.usuario }
        .stateIn(scope, SharingStarted.Eagerly, sessionManager.current?.usuario)

    override suspend fun login(login: String, senha: String): AppResult<UsuarioResumo> =
        when (val result = call.execute { api.login(LoginRequest(login.trim(), senha)) }) {
            is AppResult.Failure -> result
            is AppResult.Success -> {
                sessionManager.start(result.data)
                AppResult.Success(result.data.usuario)
            }
        }

    override suspend fun logout() {
        val refresh = sessionManager.current?.refreshToken
        // Revoga no servidor (RF 5.1.38); em falha de rede a sessão local é descartada mesmo assim.
        call.execute { api.logout(LogoutRequest(refresh)) }
        sessionManager.end()
    }

    override suspend fun esqueciSenha(email: String): AppResult<Unit> =
        call.execute { api.solicitarRecuperacao(SolicitarRecuperacaoRequest(email.trim())) }

    override suspend fun redefinirSenha(email: String, codigo: String, novaSenha: String): AppResult<Unit> =
        call.execute { api.redefinirRecuperacao(RedefinirRecuperacaoRequest(email.trim(), codigo.trim(), novaSenha)) }

    override suspend fun solicitarCodigoAlteracao(senhaAtual: String): AppResult<Unit> =
        call.execute { api.solicitarCodigoSenha(SolicitarCodigoSenhaRequest(senhaAtual)) }

    override suspend fun alterarSenha(senhaAtual: String, codigo: String, novaSenha: String, confirmacao: String): AppResult<Unit> =
        call.execute { api.alterarSenha(AlterarSenhaRequest(senhaAtual, codigo.trim(), novaSenha, confirmacao)) }
}

class RemoteAvalonRepository(
    private val api: AvalonApi,
    private val call: ApiCall,
    private val sessionManager: SessionManager,
    private val leitura: LeituraLocal? = null
) : AvalonRepository {

    private val idColaborador: Int get() = sessionManager.current?.usuario?.idColaborador ?: -1

    private fun naoDisponivel(oQue: String) =
        AppResult.Failure(AppError(AppError.NAO_IMPLEMENTADO, "$oQue ainda não possui rota no contrato da API."))

    override suspend fun dashboard(): AppResult<Dashboard> = call.execute { api.dashboard() }

    override suspend fun comunicados(): AppResult<List<Comunicado>> = AppResult.Success(emptyList())

    private fun <T> ler(resultado: AppResult<JsonElement>, leitor: (JsonElement) -> T?): AppResult<T> = when (resultado) {
        is AppResult.Failure -> resultado
        is AppResult.Success -> leitor(resultado.data)?.let { AppResult.Success(it) }
            ?: AppResult.Failure(AppError(AppError.DESCONHECIDO, "Resposta inesperada do servidor."))
    }

    // O servidor responde 404 quando a lista está vazia; para o app isso é "nenhum registro".
    private fun listaOuVazia(resultado: AppResult<JsonElement>): AppResult<JsonElement> =
        if (resultado is AppResult.Failure && resultado.error.httpStatus == 404) AppResult.Success(JsonArray()) else resultado

    override suspend fun perfil(): AppResult<Colaborador> =
        ler(call.execute { api.colaborador(idColaborador) }, LeituraApi::colaborador)

    override suspend fun solicitarCorrecao(solicitacao: SolicitacaoCorrecao): AppResult<Unit> =
        naoDisponivel("A solicitação de correção cadastral")

    override suspend fun jornada(): AppResult<Pair<JornadaEscala, List<DiaSemana>>?> {
        val colaborador = when (val r = perfil()) {
            is AppResult.Failure -> return r
            is AppResult.Success -> r.data
        }
        val idJornada = colaborador.idJornadaEscala ?: return AppResult.Success(null)
        val jornada = when (val r = ler(call.execute { api.jornada(idJornada) }, LeituraApi::jornada)) {
            is AppResult.Failure -> return r
            is AppResult.Success -> r.data
        }
        val dias = when (val r = call.execute { api.diasJornada(jornada.id) }) {
            is AppResult.Failure ->
                if (r.error.codigo == AppError.NAO_IMPLEMENTADO) call.execute { api.diasSemana() } else return r
            is AppResult.Success -> r
        }
        return ler(dias) { LeituraApi.dias(it, jornada.id) }.map { jornada to it }
    }

    // O servidor devolve as férias de todos os colaboradores e ignora filtros: o app filtra pelo titular.
    override suspend fun ferias(): AppResult<List<Ferias>> {
        val id = idColaborador
        return ler(listaOuVazia(call.execute { api.ferias() })) { LeituraApi.ferias(it, id) }
    }

    override suspend fun solicitarFerias(request: FeriasRequest): AppResult<Ferias> {
        val resultado = ler(call.execute { api.solicitarFerias(request.copy(idColaborador = idColaborador)) }, LeituraApi::feriasCriada)
        if (resultado is AppResult.Failure && resultado.error.codigo == ApiCall.SEM_PERMISSAO) {
            return AppResult.Failure(AppError(ApiCall.SEM_PERMISSAO, "O servidor ainda não aceita a solicitação de férias para o seu perfil. Procure o RH.", resultado.error.httpStatus))
        }
        return resultado
    }

    // Mostra só as concessões do titular; o catálogo (/beneficio) serve de apoio quando a concessão não traz o benefício.
    override suspend fun beneficios(): AppResult<List<BeneficioItem>> {
        val id = idColaborador
        val concessoes = when (val r = listaOuVazia(call.execute { api.concessoesBeneficio() })) {
            is AppResult.Failure -> return r
            is AppResult.Success -> r.data
        }
        val catalogo = when (val r = listaOuVazia(call.execute { api.catalogoBeneficio() })) {
            is AppResult.Failure -> return r
            is AppResult.Success -> r.data
        }
        return ler(AppResult.Success(concessoes)) { LeituraApi.beneficios(it, catalogo, id) }
    }

    override suspend fun solicitarBeneficio(item: BeneficioItem): AppResult<Unit> =
        naoDisponivel("A solicitação de adesão/cancelamento de benefício")

    override suspend fun documentos(): AppResult<List<Documento>> {
        val id = idColaborador
        return ler(listaOuVazia(call.execute { api.documentos() })) { LeituraApi.documentos(it, id) }
    }

    // GET /documento/download/{id} devolve o arquivo; lê o corpo fora da thread principal.
    override suspend fun baixarDocumento(documento: Documento): AppResult<ByteArray> {
        val resposta = call.execute { api.baixarDocumento(documento.id) }
        return when (resposta) {
            is AppResult.Failure -> resposta
            is AppResult.Success -> try {
                val bytes = withContext(Dispatchers.IO) { resposta.data.bytes() }
                if (bytes.isEmpty()) AppResult.Failure(AppError(AppError.DESCONHECIDO, "O arquivo do documento está vazio. Procure o RH."))
                else AppResult.Success(bytes)
            } catch (e: IOException) {
                AppResult.Failure(AppError(AppError.SEM_CONEXAO, "Não foi possível baixar o arquivo. Verifique sua conexão."))
            }
        }
    }

    override suspend fun feedbacks(): AppResult<List<Feedback>> {
        val id = idColaborador
        val vistos = leitura?.ids(LeituraLocal.FEEDBACK, id).orEmpty()
        return ler(listaOuVazia(call.execute { api.feedbacks() })) { LeituraApi.feedbacks(it, id, vistos) }
    }

    override suspend fun marcarFeedbackLido(idFeedback: Int) {
        leitura?.marcar(LeituraLocal.FEEDBACK, idColaborador, listOf(idFeedback))
    }

    override suspend fun respostasFeedback(idFeedback: Int): AppResult<List<RespostaFeedback>> {
        val id = idColaborador
        return ler(listaOuVazia(call.execute { api.respostasFeedback() })) { LeituraApi.respostasFeedback(it, idFeedback, id) }
    }

    /**
     * O servidor responde 201 mesmo quando a trigger do banco recusa a gravação (autor diferente do destinatário) e
     * devolve 500 com corpo HTML na duplicidade. Por isso o resultado só vale depois de reler as respostas do titular:
     * sem a releitura o app mostraria "enviada" para uma resposta que não existe.
     */
    override suspend fun responderFeedback(idFeedback: Int, texto: String): AppResult<RespostaFeedback> {
        val corpo = RespostaFeedbackNova(idFeedback, idColaborador, texto.trim())
        val envio = call.execute { api.responderFeedback(corpo) }
        if (envio is AppResult.Failure && envio.error.codigo == AppError.SEM_CONEXAO) return envio
        val recusa: AppError? = when (envio) {
            is AppResult.Failure -> envio.error
            is AppResult.Success -> LeituraApi.falhaNoCorpo(envio.data)?.let { (codigo, mensagem) -> AppError(codigo, mensagem ?: "Não foi possível enviar a resposta.") }
        }
        val gravadas = when (val r = respostasFeedback(idFeedback)) {
            is AppResult.Success -> r.data
            is AppResult.Failure -> return AppResult.Failure(AppError(r.error.codigo, "Não foi possível confirmar o envio. Atualize a tela antes de tentar de novo.", r.error.httpStatus))
        }
        if (recusa != null) {
            return if (gravadas.isNotEmpty()) AppResult.Failure(AppError("FEEDBACK_JA_RESPONDIDO", "Você já respondeu este feedback.", 409))
            else AppResult.Failure(AppError(recusa.codigo, if (recusa.httpStatus == 500) "Não foi possível enviar a resposta agora. Tente novamente." else recusa.mensagem, recusa.httpStatus))
        }
        return gravadas.lastOrNull { it.descricao.trim() == corpo.descricao }?.let { AppResult.Success(it) }
            ?: AppResult.Failure(AppError("RESPOSTA_NAO_GRAVADA", "O servidor não registrou a resposta. Confira se este feedback é destinado a você e tente de novo."))
    }

    // Leitura pela API real. O Back-end devolve as listagens de todos os colaboradores; o app filtra pelo titular.
    override suspend fun pesquisas(): AppResult<List<Pesquisa>> {
        val id = idColaborador
        val pesquisas = when (val r = listaOuVazia(call.execute { api.listaPesquisas() })) {
            is AppResult.Failure -> return r
            is AppResult.Success -> r.data
        }
        val perguntas = when (val r = listaOuVazia(call.execute { api.listaPerguntas() })) {
            is AppResult.Failure -> return r
            is AppResult.Success -> r.data
        }
        val participacoes = when (val r = listaOuVazia(call.execute { api.participacoes() })) {
            is AppResult.Failure -> return r
            is AppResult.Success -> r.data
        }
        return ler(AppResult.Success(pesquisas)) { LeituraApi.pesquisas(it, perguntas, participacoes, id) }
    }

    override suspend fun pesquisa(id: Int): AppResult<Pair<Pesquisa, List<Pergunta>>> {
        val pesquisa = when (val r = pesquisas()) {
            is AppResult.Failure -> return r
            is AppResult.Success -> r.data.firstOrNull { it.id == id }
                ?: return AppResult.Failure(AppError(AppError.DESCONHECIDO, "Pesquisa não encontrada para o seu usuário."))
        }
        val perguntas = when (val r = ler(listaOuVazia(call.execute { api.listaPerguntas() })) { LeituraApi.perguntas(it, id) }) {
            is AppResult.Failure -> return r
            is AppResult.Success -> r.data
        }
        if (perguntas.isEmpty()) return AppResult.Failure(AppError(AppError.DESCONHECIDO, "Esta pesquisa ainda não possui perguntas."))
        return AppResult.Success(pesquisa to perguntas)
    }

    // POST /resposta e POST /participacao respondem 403 para o perfil Colaborador (só o RH grava).
    override suspend fun responderPesquisa(id: Int, respostas: List<RespostaPesquisaItem>): AppResult<RespostaConfirmacao> =
        when (val r = call.execute { api.responderPesquisa(id, RespostaPesquisaLote(respostas)) }) {
            is AppResult.Failure -> r
            is AppResult.Success -> LeituraApi.falhaNoCorpo(r.data)
                ?.let { (codigo, mensagem) ->
                    val texto = if (codigo == "PESQUISA_JA_RESPONDIDA") "Você já respondeu este questionário." else mensagem ?: "Não foi possível enviar as respostas."
                    AppResult.Failure(AppError(codigo, texto, if (codigo == "PESQUISA_JA_RESPONDIDA") 409 else null))
                }
                ?: AppResult.Success(RespostaConfirmacao(respostas.size, true))
        }

    override suspend fun avaliacaoPendente(): AppResult<Avaliacao?> =
        call.execute { api.avaliacoes(somentePendentes = true) }.map { it.firstOrNull() }

    override suspend fun perguntasAvaliacao(id: Int): AppResult<List<AvaliacaoPergunta>> =
        call.execute { api.perguntasAvaliacao(id) }.map { lista -> lista.filter { it.ativo }.sortedWith(compareBy({ it.idFator }, { it.ordem })) }

    override suspend fun responderAvaliacao(id: Int, respostas: List<RespostaAvaliacaoItem>): AppResult<RespostaConfirmacao> =
        call.execute { api.responderAvaliacao(id, RespostaAvaliacaoLote(respostas)) }

    override suspend fun notificacoes(): AppResult<List<Notificacao>> {
        val id = idColaborador
        val lidas = leitura?.ids(LeituraLocal.NOTIFICACAO, id).orEmpty()
        return ler(listaOuVazia(call.execute { api.notificacoes() })) { LeituraApi.notificacoes(it, id, lidas) }
    }

    override suspend fun marcarNotificacoesLidas(ids: List<Int>) {
        leitura?.marcar(LeituraLocal.NOTIFICACAO, idColaborador, ids)
    }
}
