package com.example.avalon_mobile.data.repository

import com.example.avalon_mobile.data.AppError
import com.example.avalon_mobile.data.AppResult
import com.example.avalon_mobile.data.local.SessionManager
import com.example.avalon_mobile.model.Avaliacao
import com.example.avalon_mobile.model.AvaliacaoPergunta
import com.example.avalon_mobile.model.Beneficio
import com.example.avalon_mobile.model.BeneficioItem
import com.example.avalon_mobile.model.Colaborador
import com.example.avalon_mobile.model.Comunicado
import com.example.avalon_mobile.model.Dashboard
import com.example.avalon_mobile.model.DashboardColaborador
import com.example.avalon_mobile.model.DiaSemana
import com.example.avalon_mobile.model.Documento
import com.example.avalon_mobile.model.Feedback
import com.example.avalon_mobile.model.Ferias
import com.example.avalon_mobile.model.FeriasRequest
import com.example.avalon_mobile.model.JornadaEscala
import com.example.avalon_mobile.model.NivelAcesso
import com.example.avalon_mobile.model.Notificacao
import com.example.avalon_mobile.model.Pergunta
import com.example.avalon_mobile.model.Pesquisa
import com.example.avalon_mobile.model.RespostaAvaliacaoItem
import com.example.avalon_mobile.model.RespostaConfirmacao
import com.example.avalon_mobile.model.RespostaFeedback
import com.example.avalon_mobile.model.RespostaPesquisaItem
import com.example.avalon_mobile.model.SolicitacaoCorrecao
import com.example.avalon_mobile.model.TokenResponse
import com.example.avalon_mobile.model.UsuarioResumo
import com.example.avalon_mobile.util.PdfFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.util.UUID

/**
 * Autenticação de demonstração (BuildConfig.USE_MOCK_API = true), com as mesmas regras do contrato:
 * - ana.lucia@empresa.com.br / 123456 → sucesso
 * - inativo@empresa.com.br / 123456 → 403 CONTA_INATIVA (só depois de a senha conferir)
 * - qualquer outro     → 401 CREDENCIAIS_INVALIDAS (mesma resposta para login inexistente e senha errada)
 * A sessão passa pelo mesmo armazenamento cifrado da versão real.
 */
class MockAuthRepository(
    private val sessionManager: SessionManager,
    scope: CoroutineScope
) : AuthRepository {

    override val usuario: StateFlow<UsuarioResumo?> = sessionManager.session
        .map { it?.usuario }
        .stateIn(scope, SharingStarted.Eagerly, sessionManager.current?.usuario)

    override suspend fun login(login: String, senha: String): AppResult<UsuarioResumo> {
        delay(LATENCIA)
        val user = login.trim().lowercase()
        if (senha != SENHA_DEMO || user !in setOf(LOGIN_DEMO, LOGIN_INATIVO)) {
            return AppResult.Failure(AppError("CREDENCIAIS_INVALIDAS", "E-mail ou senha inválidos.", 401))
        }
        if (user == LOGIN_INATIVO) {
            return AppResult.Failure(AppError("CONTA_INATIVA", "Esta conta está inativa. Procure o RH.", 403))
        }
        val usuario = UsuarioResumo(7, LOGIN_DEMO, MockData.colaborador.id, MockData.colaborador.nome, NivelAcesso(NivelAcesso.COLABORADOR, "Colaborador"))
        sessionManager.start(
            TokenResponse("mock-${UUID.randomUUID()}", "Bearer", 900, "rt_mock_${UUID.randomUUID()}", usuario, emptyList())
        )
        return AppResult.Success(usuario)
    }

    override suspend fun logout() {
        sessionManager.end()
    }

    override suspend fun esqueciSenha(login: String): AppResult<Unit> {
        delay(LATENCIA)
        return AppResult.Success(Unit) // sempre 202, exista ou não o login
    }

    override suspend fun redefinirSenha(token: String, novaSenha: String, confirmacao: String): AppResult<Unit> {
        delay(LATENCIA)
        return if (token.trim().length == 4) AppResult.Success(Unit)
        else AppResult.Failure(AppError("TOKEN_REDEFINICAO_INVALIDO", "O código expirou ou já foi utilizado.", 422))
    }

    private var errosCodigo = 0

    override suspend fun solicitarCodigoAlteracao(senhaAtual: String): AppResult<Unit> {
        delay(LATENCIA)
        return if (senhaAtual == SENHA_DEMO) AppResult.Success(Unit)
        else AppResult.Failure(AppError("SENHA_ATUAL_INCORRETA", "A senha atual está incorreta.", 422))
    }

    override suspend fun alterarSenha(senhaAtual: String, codigo: String, novaSenha: String, confirmacao: String): AppResult<Unit> {
        delay(LATENCIA)
        if (senhaAtual != SENHA_DEMO) return AppResult.Failure(AppError("SENHA_ATUAL_INCORRETA", "A senha atual está incorreta.", 422))
        if (errosCodigo >= LIMITE_ERROS_CODIGO) return contaBloqueada()
        if (codigo.trim() != CODIGO_DEMO) {
            errosCodigo++
            return if (errosCodigo >= LIMITE_ERROS_CODIGO) contaBloqueada()
            else AppResult.Failure(AppError("CODIGO_INVALIDO", "Código inválido ou expirado.", 422))
        }
        errosCodigo = 0
        return AppResult.Success(Unit)
    }

    private fun contaBloqueada() = AppResult.Failure(
        AppError("CONTA_BLOQUEADA", "Conta bloqueada por excesso de tentativas. Procure o RH para redefinir sua senha.", 423)
    )

    companion object {
        const val LOGIN_DEMO = "ana.lucia@empresa.com.br"
        const val LOGIN_INATIVO = "inativo@empresa.com.br"
        const val SENHA_DEMO = "123456"
        const val CODIGO_DEMO = "1234"
        const val LIMITE_ERROS_CODIGO = 4
        const val LATENCIA = 500L
    }
}

class MockAvalonRepository(
    private val pdfFactory: PdfFactory,
    private val usuarioLogado: () -> UsuarioResumo? = { null }
) : AvalonRepository {

    private val ferias = MockData.ferias.toMutableList()
    private val respostasFeedback = mutableMapOf<Int, MutableList<RespostaFeedback>>()
    private val pesquisasRespondidas = mutableSetOf<Int>()
    private var avaliacaoRespondida = false

    private suspend fun <T> ok(data: T): AppResult<T> {
        delay(MockAuthRepository.LATENCIA / 2)
        return AppResult.Success(data)
    }

    override suspend fun dashboard() = ok(
        Dashboard(
            perfil = "Colaborador",
            colaborador = DashboardColaborador(
                feriasEmAndamento = ferias.count { it.status == "Pendente" || it.status == "Aprovado pelo Gestor" },
                pesquisasPendentes = MockData.pesquisas.count { it.respondida == false && it.id !in pesquisasRespondidas },
                avaliacoesPendentes = if (avaliacaoRespondida) 0 else 1,
                notificacoesTotal = MockData.notificacoes.size
            )
        )
    )

    override suspend fun comunicados() = ok(MockData.comunicados)

    override suspend fun perfil() = ok(
        usuarioLogado()
            ?.let { MockData.colaborador.copy(id = it.idColaborador, nome = it.nome) }
            ?: MockData.colaborador
    )

    override suspend fun solicitarCorrecao(solicitacao: SolicitacaoCorrecao) = ok(Unit)

    override suspend fun jornada() = ok(MockData.jornada to MockData.dias)

    override suspend fun ferias() = ok(ferias.sortedByDescending { it.dataInicio })

    override suspend fun solicitarFerias(request: FeriasRequest): AppResult<Ferias> {
        val inicio = LocalDate.parse(request.dataInicio)
        val fim = LocalDate.parse(request.dataFim)
        val conflito = ferias.any {
            it.status?.startsWith("Recusado") != true &&
                !(fim.isBefore(LocalDate.parse(it.dataInicio)) || inicio.isAfter(LocalDate.parse(it.dataFim)))
        }
        if (conflito) return AppResult.Failure(AppError("FERIAS_SOBREPOSTAS", "Já existe um pedido de férias nesse período.", 409))
        val nova = Ferias(
            id = (ferias.maxOfOrNull { it.id } ?: 0) + 1,
            dataInicio = request.dataInicio,
            dataFim = request.dataFim,
            quantidadeDias = request.quantidadeDias,
            observacao = request.observacao,
            idColaborador = MockData.colaborador.id,
            status = "Pendente",
            dataSolicitacao = LocalDate.now().toString()
        )
        ferias += nova
        return ok(nova)
    }

    override suspend fun beneficios() = ok(MockData.beneficios)

    override suspend fun solicitarBeneficio(item: BeneficioItem) = ok(Unit)

    override suspend fun documentos() = ok(MockData.documentos)

    override suspend fun baixarDocumento(documento: Documento): AppResult<ByteArray> =
        ok(pdfFactory.documentoDemonstracao(documento.nome, documento.tipoDocumento))

    override suspend fun feedbacks() = ok(MockData.feedbacks)

    override suspend fun respostasFeedback(idFeedback: Int) = ok(respostasFeedback[idFeedback].orEmpty().toList())

    override suspend fun responderFeedback(idFeedback: Int, texto: String): AppResult<RespostaFeedback> {
        val lista = respostasFeedback.getOrPut(idFeedback) { mutableListOf() }
        val resposta = RespostaFeedback(lista.size + 1, texto.trim(), idFeedback, MockData.colaborador.id, LocalDate.now().toString())
        lista += resposta
        return ok(resposta)
    }

    override suspend fun pesquisas() = ok(
        MockData.pesquisas.map { if (it.id in pesquisasRespondidas) it.copy(respondida = true, dataResposta = LocalDate.now().toString()) else it }
    )

    override suspend fun pesquisa(id: Int): AppResult<Pair<Pesquisa, List<Pergunta>>> {
        val pesquisa = MockData.pesquisas.firstOrNull { it.id == id }
            ?: return AppResult.Failure(AppError("RECURSO_NAO_ENCONTRADO", "Pesquisa não encontrada.", 404))
        return ok(pesquisa to MockData.perguntas(id))
    }

    override suspend fun responderPesquisa(id: Int, respostas: List<RespostaPesquisaItem>): AppResult<RespostaConfirmacao> {
        if (id in pesquisasRespondidas) return AppResult.Failure(AppError("PESQUISA_JA_RESPONDIDA", "Você já respondeu este questionário.", 409))
        val obrigatorias = MockData.perguntas(id).filter { it.obrigatorio }.map { it.id }.toSet()
        if (!respostas.map { it.idPergunta }.containsAll(obrigatorias)) {
            return AppResult.Failure(AppError("RESPOSTA_OBRIGATORIA_AUSENTE", "Responda todas as perguntas obrigatórias.", 422))
        }
        pesquisasRespondidas += id
        return ok(RespostaConfirmacao(respostas.size, true))
    }

    override suspend fun avaliacaoPendente() = ok(if (avaliacaoRespondida) null else MockData.avaliacao)

    override suspend fun perguntasAvaliacao(id: Int) = ok(MockData.perguntasAvaliacao)

    override suspend fun responderAvaliacao(id: Int, respostas: List<RespostaAvaliacaoItem>): AppResult<RespostaConfirmacao> {
        if (avaliacaoRespondida) return AppResult.Failure(AppError("AVALIACAO_JA_RESPONDIDA", "Você já respondeu este questionário.", 409))
        avaliacaoRespondida = true
        return ok(RespostaConfirmacao(respostas.size, true))
    }

    override suspend fun notificacoes() = ok(MockData.notificacoes)
}

/** Massa de demonstração com os textos do protótipo no Figma. */
private object MockData {
    val colaborador = Colaborador(
        id = 4, idSetor = 1, idCargo = 1, idJornadaEscala = 1,
        matricula = "AV-02841", nome = "Ana Lucia", cpf = "12348278910",
        email = "ana@empresa.com.br", dataAdmissao = "2025-03-10", status = "Ativo", tipoVinculo = "CLT",
        setorNome = "Tecnologia da Informação (TI)", cargoNome = "Desenvolvedora Full Stack"
    )

    val jornada = JornadaEscala(1, "Turno administrativo", "Segunda a sexta", "08:00:00", "17:00:00")

    val dias = listOf(
        DiaSemana(1, "Segunda-feira", "SEG", true), DiaSemana(2, "Terça-feira", "TER", true),
        DiaSemana(3, "Quarta-feira", "QUA", true), DiaSemana(4, "Quinta-feira", "QUI", true),
        DiaSemana(5, "Sexta-feira", "SEX", true), DiaSemana(6, "Sábado", "SAB", false),
        DiaSemana(7, "Domingo", "DOM", false)
    )

    val ferias = listOf(
        Ferias(1, "2026-12-15", "2026-12-24", 10, null, 4, "Pendente", "2026-09-01"),
        Ferias(2, "2026-06-10", "2026-06-24", 15, null, 4, "Aprovado pelo RH", "2026-05-15"),
        Ferias(3, "2026-03-02", "2026-03-06", 5, null, 4, "Recusado pelo Gestor", "2026-02-10")
    )

    val beneficios = listOf(
        BeneficioItem(Beneficio(1, "Plano de Saúde Unimed", "Cobertura nacional, internação em apartamento. Inclusos consultas e exames preventivos."), true),
        BeneficioItem(Beneficio(2, "Vale Alimentação", "Crédito mensal de R$ 850,00 aceito em redes de supermercados nacionais • Sodexo."), true),
        BeneficioItem(Beneficio(3, "Seguro de Vida", "Cobertura securitária completa no valor de R$ 500.000 para acidentes pessoais."), true),
        BeneficioItem(Beneficio(4, "Gympass", "Acesso ilimitado a milhares de academias parceiras, estúdios e apps de saúde digital."), false),
        BeneficioItem(Beneficio(5, "Previdência Privada", "Fundo de previdência corporativa fechado com contrapartida de 100% match da empresa."), false)
    )

    val documentos = listOf(
        Documento(1, 4, "Contrato de Trabalho", "Contrato", dataDisponibilizacao = "2024-03-15", tamanhoBytes = 420_000, lido = true),
        Documento(2, 4, "Termo de Confidencialidade", "Termo", dataDisponibilizacao = "2024-03-15", tamanhoBytes = 180_000, lido = true),
        Documento(3, 4, "Acordo de Home Office", "Aditivo Contratual", dataDisponibilizacao = "2025-06-02", tamanhoBytes = 150_000, lido = true),
        Documento(4, 4, "Extrato de Benefícios — Agosto 2026", "Comunicação", dataDisponibilizacao = "2026-09-05", tamanhoBytes = 1_200_000, lido = true),
        Documento(5, 4, "Contracheque — Agosto 2026", "Recibo", dataDisponibilizacao = "2026-09-05", tamanhoBytes = 3_400_000, lido = true),
        Documento(6, 4, "Escala Trabalhada — Agosto 2026", "Documento", dataDisponibilizacao = "2026-09-05", tamanhoBytes = 890_000, lido = false),
        Documento(7, 4, "Política de Privacidade (LGPD)", "Política", ativo = false, dataDisponibilizacao = "2025-01-10")
    )

    val feedbacks = listOf(
        Feedback(1, 4, "Reconhecimento", "Ótimo atendimento no caso do cliente da conta digital na última quarta. A resolução foi rápida, clara e o cliente elogiou o cuidado na explicação. Continue assim.", "2026-09-05", true, 2, "Renata Costa", "Supervisora", lido = false),
        Feedback(2, 4, "Desenvolvimento", "Vamos alinhar pontos de melhoria no tempo médio de resposta dos tickets. Sugiro reservarmos 30 minutos na próxima semana para revisar a fila.", "2026-08-28", true, 3, "João Pires", "Coordenador", lido = true),
        Feedback(3, 4, "Formal", "Registro de avaliação de experiência — período de 90 dias concluído com resultado satisfatório.", "2026-07-10", true, 1, "Recursos Humanos", "Formal", lido = true)
    )

    val pesquisas = listOf(
        Pesquisa(1, "Pesquisa de clima organizacional", null, "2026-10-01", "2026-10-20", anonimo = false, status = true, totalPerguntas = 8, respondida = false),
        Pesquisa(2, "Pesquisa de experiência do colaborador", null, "2026-10-05", "2026-10-30", anonimo = true, status = true, totalPerguntas = 4, respondida = false),
        Pesquisa(3, "Avaliação de liderança", null, "2026-07-20", "2026-08-10", anonimo = true, status = true, totalPerguntas = 6, respondida = true, dataResposta = "2026-08-02"),
        Pesquisa(4, "Pesquisa de satisfação Q2", null, "2026-06-01", "2026-06-30", anonimo = true, status = false, totalPerguntas = 5, respondida = false)
    )

    fun perguntas(idPesquisa: Int): List<Pergunta> = when (idPesquisa) {
        1 -> listOf(
            Pergunta(11, "Você se sente reconhecido pelo trabalho que realiza?", "ESCALA", 1, true, 1),
            Pergunta(12, "Você se sente à vontade para expressar suas opiniões no time?", "ESCALA", 2, true, 1),
            Pergunta(13, "A comunicação entre as áreas é clara?", "ESCALA", 3, true, 1),
            Pergunta(14, "Você tem os recursos necessários para realizar seu trabalho?", "ESCALA", 4, true, 1),
            Pergunta(15, "Você recomendaria a empresa como um bom lugar para trabalhar?", "ESCALA", 5, true, 1),
            Pergunta(16, "Você sente que sua carga de trabalho é adequada?", "ESCALA", 6, false, 1),
            Pergunta(17, "Você se sente apoiado pela sua liderança direta?", "ESCALA", 7, false, 1),
            Pergunta(18, "O que poderia melhorar no seu dia a dia?", "TEXTO", 8, false, 1)
        )
        2 -> listOf(
            Pergunta(21, "Você sente que sua carga de trabalho é adequada?", "ESCALA", 1, true, 2),
            Pergunta(22, "Você já sentiu esgotamento relacionado ao trabalho nos últimos 3 meses?", "ESCALA", 2, true, 2),
            Pergunta(23, "Você se sente apoiado pela sua liderança direta?", "ESCALA", 3, true, 2),
            Pergunta(24, "Deixe um comentário (opcional).", "TEXTO", 4, false, 2)
        )
        else -> emptyList()
    }

    val avaliacao = Avaliacao(1, "Fatores de risco psicossocial - NR-1", "Esta avaliação faz parte das ações da empresa para acompanhar o ambiente de trabalho, conforme a NR-1. Leva cerca de 5 minutos.", "2026-10-01", "2026-10-30", anonimo = true, status = true)

    val perguntasAvaliacao = listOf(
        "Nas últimas semanas, com que frequência você se sentiu esgotado ao final do dia de trabalho?",
        "Com que frequência você precisa trabalhar em ritmo muito acelerado?",
        "Com que frequência você tem prazos difíceis de cumprir?",
        "Com que frequência você consegue fazer pausas durante a jornada?",
        "Com que frequência você tem autonomia para organizar suas tarefas?",
        "Com que frequência você recebe apoio dos colegas quando precisa?",
        "Com que frequência você recebe apoio da liderança quando precisa?",
        "Com que frequência você presencia conflitos no ambiente de trabalho?",
        "Com que frequência você sente que suas tarefas estão claras?",
        "Com que frequência você pensa no trabalho fora do horário?",
        "Com que frequência você se sente reconhecido pelo que faz?",
        "Com que frequência você se sente seguro para relatar problemas?"
    ).mapIndexed { i, texto -> AvaliacaoPergunta(100 + i, idFator = i / 2 + 1, enunciado = texto, ordem = i % 2 + 1, obrigatorio = true) }

    val notificacoes = listOf(
        Notificacao(4, 4, "PESQUISA", "Nova pesquisa disponível", "Pesquisa de clima organizacional — responda até 20/10.", lida = false, dataHora = "Hoje, 09:12"),
        Notificacao(3, 4, "FERIAS", "Férias em análise", "Seu pedido de 15 a 24/12 foi encaminhado ao gestor.", lida = false, dataHora = "Hoje, 08:40"),
        Notificacao(2, 4, "FEEDBACK", "Novo feedback recebido", "Renata Costa enviou um reconhecimento.", lida = true, dataHora = "Ontem, 17:25"),
        Notificacao(1, 4, "DOCUMENTO", "Documento disponível", "Contracheque — Agosto 2026 já pode ser baixado.", lida = true, dataHora = "Ontem, 10:03")
    )

    val comunicados = listOf(
        Comunicado(1, "Encontro mensal do time", "Dia 18, às 16h • Auditório principal")
    )
}
