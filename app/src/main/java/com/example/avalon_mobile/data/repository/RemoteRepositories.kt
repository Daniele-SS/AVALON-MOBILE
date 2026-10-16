package com.example.avalon_mobile.data.repository

import com.example.avalon_mobile.data.AppError
import com.example.avalon_mobile.data.AppResult
import com.example.avalon_mobile.data.local.SessionManager
import com.example.avalon_mobile.data.map
import com.example.avalon_mobile.data.remote.ApiCall
import com.example.avalon_mobile.data.remote.AvalonApi
import com.example.avalon_mobile.model.Avaliacao
import com.example.avalon_mobile.model.AvaliacaoPergunta
import com.example.avalon_mobile.model.BeneficioItem
import com.example.avalon_mobile.model.Colaborador
import com.example.avalon_mobile.model.Comunicado
import com.example.avalon_mobile.model.Dashboard
import com.example.avalon_mobile.model.DiaSemana
import com.example.avalon_mobile.model.Documento
import com.example.avalon_mobile.model.EsqueciSenhaRequest
import com.example.avalon_mobile.model.Feedback
import com.example.avalon_mobile.model.Ferias
import com.example.avalon_mobile.model.FeriasRequest
import com.example.avalon_mobile.model.JornadaEscala
import com.example.avalon_mobile.model.LoginRequest
import com.example.avalon_mobile.model.LogoutRequest
import com.example.avalon_mobile.model.Notificacao
import com.example.avalon_mobile.model.Pergunta
import com.example.avalon_mobile.model.Pesquisa
import com.example.avalon_mobile.model.RedefinirSenhaRequest
import com.example.avalon_mobile.model.RespostaAvaliacaoItem
import com.example.avalon_mobile.model.RespostaAvaliacaoLote
import com.example.avalon_mobile.model.RespostaConfirmacao
import com.example.avalon_mobile.model.RespostaFeedback
import com.example.avalon_mobile.model.RespostaFeedbackRequest
import com.example.avalon_mobile.model.RespostaPesquisaItem
import com.example.avalon_mobile.model.RespostaPesquisaLote
import com.example.avalon_mobile.model.SolicitacaoCorrecao
import com.example.avalon_mobile.model.UsuarioResumo
import kotlinx.coroutines.CoroutineScope
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

    override suspend fun esqueciSenha(login: String): AppResult<Unit> =
        call.execute { api.esqueciSenha(EsqueciSenhaRequest(login.trim())) }

    override suspend fun redefinirSenha(token: String, novaSenha: String, confirmacao: String): AppResult<Unit> =
        call.execute { api.redefinirSenha(RedefinirSenhaRequest(token.trim(), novaSenha, confirmacao)) }
}

class RemoteAvalonRepository(
    private val api: AvalonApi,
    private val call: ApiCall,
    private val sessionManager: SessionManager
) : AvalonRepository {

    private val idColaborador: Int get() = sessionManager.current?.usuario?.idColaborador ?: -1

    private fun naoDisponivel(oQue: String) =
        AppResult.Failure(AppError(AppError.NAO_IMPLEMENTADO, "$oQue ainda não possui rota no contrato da API."))

    override suspend fun dashboard(): AppResult<Dashboard> = call.execute { api.dashboard() }

    override suspend fun comunicados(): AppResult<List<Comunicado>> = AppResult.Success(emptyList())

    override suspend fun perfil(): AppResult<Colaborador> = call.execute { api.colaborador(idColaborador) }

    override suspend fun solicitarCorrecao(solicitacao: SolicitacaoCorrecao): AppResult<Unit> =
        naoDisponivel("A solicitação de correção cadastral")

    override suspend fun jornada(): AppResult<Pair<JornadaEscala, List<DiaSemana>>> {
        val colaborador = when (val r = perfil()) {
            is AppResult.Failure -> return r
            is AppResult.Success -> r.data
        }
        val jornada = when (val r = call.execute { api.jornada(colaborador.idJornadaEscala) }) {
            is AppResult.Failure -> return r
            is AppResult.Success -> r.data
        }
        return call.execute { api.diasJornada(jornada.id) }.map { jornada to it }
    }

    override suspend fun ferias(): AppResult<List<Ferias>> = call.execute { api.ferias(idColaborador) }

    override suspend fun solicitarFerias(request: FeriasRequest): AppResult<Ferias> =
        call.execute { api.solicitarFerias(request) }

    override suspend fun beneficios(): AppResult<List<BeneficioItem>> =
        call.execute { api.beneficiosDoColaborador(idColaborador) }.map { concessoes ->
            // O catálogo completo (/beneficio) é restrito ao RH: o Colaborador vê só as concessões.
            concessoes.mapNotNull { c -> c.beneficio?.let { BeneficioItem(it, aderido = c.ativo) } }
        }

    override suspend fun solicitarBeneficio(item: BeneficioItem): AppResult<Unit> =
        naoDisponivel("A solicitação de adesão/cancelamento de benefício")

    override suspend fun documentos(): AppResult<List<Documento>> = call.execute { api.documentos(idColaborador) }

    override suspend fun baixarDocumento(documento: Documento): AppResult<ByteArray> =
        call.execute { api.baixarDocumento(documento.id) }.map { it.bytes() }

    override suspend fun feedbacks(): AppResult<List<Feedback>> = call.execute { api.feedbacks(idColaborador) }

    override suspend fun respostasFeedback(idFeedback: Int): AppResult<List<RespostaFeedback>> =
        call.execute { api.respostasFeedback(idFeedback) }

    override suspend fun responderFeedback(idFeedback: Int, texto: String): AppResult<RespostaFeedback> =
        call.execute { api.responderFeedback(idFeedback, RespostaFeedbackRequest(texto.trim())) }

    override suspend fun pesquisas(): AppResult<List<Pesquisa>> {
        val todas = when (val r = call.execute { api.pesquisas() }) {
            is AppResult.Failure -> return r
            is AppResult.Success -> r.data
        }
        val pendentes = when (val r = call.execute { api.pesquisas(somentePendentes = true) }) {
            is AppResult.Failure -> return r
            is AppResult.Success -> r.data.map { it.id }.toSet()
        }
        return AppResult.Success(todas.map { it.copy(respondida = it.respondida ?: (it.id !in pendentes)) })
    }

    override suspend fun pesquisa(id: Int): AppResult<Pair<Pesquisa, List<Pergunta>>> {
        val pesquisa = when (val r = call.execute { api.pesquisa(id) }) {
            is AppResult.Failure -> return r
            is AppResult.Success -> r.data
        }
        return call.execute { api.perguntasPesquisa(id) }.map { perguntas -> pesquisa to perguntas.sortedBy { it.ordem } }
    }

    override suspend fun responderPesquisa(id: Int, respostas: List<RespostaPesquisaItem>): AppResult<RespostaConfirmacao> =
        call.execute { api.responderPesquisa(id, RespostaPesquisaLote(respostas)) }

    override suspend fun avaliacaoPendente(): AppResult<Avaliacao?> =
        call.execute { api.avaliacoes(somentePendentes = true) }.map { it.firstOrNull() }

    override suspend fun perguntasAvaliacao(id: Int): AppResult<List<AvaliacaoPergunta>> =
        call.execute { api.perguntasAvaliacao(id) }.map { lista -> lista.filter { it.ativo }.sortedWith(compareBy({ it.idFator }, { it.ordem })) }

    override suspend fun responderAvaliacao(id: Int, respostas: List<RespostaAvaliacaoItem>): AppResult<RespostaConfirmacao> =
        call.execute { api.responderAvaliacao(id, RespostaAvaliacaoLote(respostas)) }

    override suspend fun notificacoes(): AppResult<List<Notificacao>> = call.execute { api.notificacoes() }
}
