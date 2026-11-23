package com.example.avalon_mobile.data.repository

import com.example.avalon_mobile.data.AppResult
import com.example.avalon_mobile.model.Colaborador
import com.example.avalon_mobile.model.BeneficioItem
import com.example.avalon_mobile.model.Dashboard
import com.example.avalon_mobile.model.DashboardColaborador
import com.example.avalon_mobile.model.DiaSemana
import com.example.avalon_mobile.model.Documento
import com.example.avalon_mobile.model.Feedback
import com.example.avalon_mobile.model.Notificacao
import com.example.avalon_mobile.model.RespostaFeedback
import com.example.avalon_mobile.model.Ferias
import com.example.avalon_mobile.model.FeriasRequest
import com.example.avalon_mobile.model.JornadaEscala
import com.example.avalon_mobile.model.Avaliacao
import com.example.avalon_mobile.model.AvaliacaoPergunta
import com.example.avalon_mobile.model.Pergunta
import com.example.avalon_mobile.model.Pesquisa
import com.example.avalon_mobile.model.RespostaAvaliacaoItem
import com.example.avalon_mobile.model.RespostaConfirmacao
import com.example.avalon_mobile.model.RespostaPesquisaItem

class AvalonRepositoryMisto(
    private val api: AvalonRepository,
    private val demonstracao: AvalonRepository,
    private val modulosApi: Set<String>
) : AvalonRepository by demonstracao {

    private fun usaApi(modulo: String) = modulo in modulosApi

    override val homeSoDadosReais: Boolean get() = usaApi(FERIAS)

    override suspend fun notificacoes(): AppResult<List<Notificacao>> = when {
        usaApi(NOTIFICACAO) -> api.notificacoes()
        homeSoDadosReais -> AppResult.Success(emptyList())
        else -> demonstracao.notificacoes()
    }

    override suspend fun marcarNotificacoesLidas(ids: List<Int>) {
        if (usaApi(NOTIFICACAO)) api.marcarNotificacoesLidas(ids) else demonstracao.marcarNotificacoesLidas(ids)
    }

    override suspend fun feedbacks(): AppResult<List<Feedback>> =
        if (usaApi(FEEDBACK)) api.feedbacks() else demonstracao.feedbacks()

    override suspend fun marcarFeedbackLido(idFeedback: Int) {
        if (usaApi(FEEDBACK)) api.marcarFeedbackLido(idFeedback) else demonstracao.marcarFeedbackLido(idFeedback)
    }

    override suspend fun respostasFeedback(idFeedback: Int): AppResult<List<RespostaFeedback>> =
        if (usaApi(FEEDBACK)) api.respostasFeedback(idFeedback) else demonstracao.respostasFeedback(idFeedback)

    override suspend fun responderFeedback(idFeedback: Int, texto: String): AppResult<RespostaFeedback> =
        if (usaApi(FEEDBACK)) api.responderFeedback(idFeedback, texto) else demonstracao.responderFeedback(idFeedback, texto)

    override suspend fun dashboard(): AppResult<Dashboard> {
        if (!usaApi(FERIAS) && !usaApi(PESQUISA)) return demonstracao.dashboard()
        val demo = (demonstracao.dashboard() as? AppResult.Success)?.data?.colaborador
        val ferias = if (usaApi(FERIAS)) {
            when (val r = api.ferias()) {
                is AppResult.Failure -> return r
                is AppResult.Success -> r.data.count { it.status == "Pendente" || it.status == "Aprovado pelo Gestor" }
            }
        } else demo?.feriasEmAndamento ?: 0
        // Com Pesquisas pela API, o contador vem das participações reais; a avaliação psicossocial conta 1 quando há uma pendente (falha na leitura = 0).
        // Se a leitura falhar, a Home mostra 0 em vez de falhar inteira (a tela Pesquisas exibe o erro).
        val pesquisas = if (usaApi(PESQUISA)) {
            (api.pesquisas() as? AppResult.Success)?.data?.count { it.estaPendente() } ?: 0
        } else demo?.pesquisasPendentes ?: 0
        val avaliacoes = if (usaApi(PESQUISA)) ((api.avaliacaoPendente() as? AppResult.Success)?.data?.let { 1 } ?: 0) else demo?.avaliacoesPendentes ?: 0
        return AppResult.Success(
            Dashboard(
                perfil = "Colaborador",
                colaborador = DashboardColaborador(
                    feriasEmAndamento = ferias,
                    pesquisasPendentes = pesquisas,
                    avaliacoesPendentes = avaliacoes
                )
            )
        )
    }

    override suspend fun pesquisas(): AppResult<List<Pesquisa>> =
        if (usaApi(PESQUISA)) api.pesquisas() else demonstracao.pesquisas()

    override suspend fun pesquisa(id: Int): AppResult<Pair<Pesquisa, List<Pergunta>>> =
        if (usaApi(PESQUISA)) api.pesquisa(id) else demonstracao.pesquisa(id)

    override suspend fun responderPesquisa(id: Int, respostas: List<RespostaPesquisaItem>): AppResult<RespostaConfirmacao> =
        if (usaApi(PESQUISA)) api.responderPesquisa(id, respostas) else demonstracao.responderPesquisa(id, respostas)

    // Avaliação psicossocial (NR-1): com Pesquisas reais, lê as rotas abertas do Back-end (avaliacao, fator, pergunta).
    // O envio ainda não tem rota para o Colaborador e volta como erro claro; nunca cai na demonstração, que fingiria sucesso.
    override suspend fun avaliacaoPendente(): AppResult<Avaliacao?> =
        if (usaApi(PESQUISA)) api.avaliacaoPendente() else demonstracao.avaliacaoPendente()

    override suspend fun perguntasAvaliacao(id: Int): AppResult<List<AvaliacaoPergunta>> =
        if (usaApi(PESQUISA)) api.perguntasAvaliacao(id) else demonstracao.perguntasAvaliacao(id)

    override suspend fun responderAvaliacao(id: Int, respostas: List<RespostaAvaliacaoItem>): AppResult<RespostaConfirmacao> =
        if (usaApi(PESQUISA)) api.responderAvaliacao(id, respostas) else demonstracao.responderAvaliacao(id, respostas)

    override suspend fun perfil(): AppResult<Colaborador> =
        if (usaApi(PERFIL)) api.perfil() else demonstracao.perfil()

    override suspend fun jornada(): AppResult<Pair<JornadaEscala, List<DiaSemana>>?> =
        if (usaApi(JORNADA)) api.jornada() else demonstracao.jornada()

    override suspend fun ferias(): AppResult<List<Ferias>> =
        if (usaApi(FERIAS)) api.ferias() else demonstracao.ferias()

    override suspend fun solicitarFerias(request: FeriasRequest): AppResult<Ferias> =
        if (usaApi(FERIAS)) api.solicitarFerias(request) else demonstracao.solicitarFerias(request)

    override suspend fun beneficios(): AppResult<List<BeneficioItem>> =
        if (usaApi(BENEFICIO)) api.beneficios() else demonstracao.beneficios()

    override suspend fun documentos(): AppResult<List<Documento>> =
        if (usaApi(DOCUMENTO)) api.documentos() else demonstracao.documentos()

    override suspend fun baixarDocumento(documento: Documento): AppResult<ByteArray> =
        if (usaApi(DOCUMENTO)) api.baixarDocumento(documento) else demonstracao.baixarDocumento(documento)

    companion object {
        const val PERFIL = "perfil"
        const val JORNADA = "jornada"
        const val FERIAS = "ferias"
        const val BENEFICIO = "beneficio"
        const val DOCUMENTO = "documento"
        const val PESQUISA = "pesquisa"
        const val FEEDBACK = "feedback"
        const val NOTIFICACAO = "notificacao"

        fun modulos(texto: String): Set<String> =
            texto.split(',').map { it.trim().lowercase() }.filter { it.isNotEmpty() }.toSet()
    }
}
