package com.example.avalon_mobile.data.repository

import com.example.avalon_mobile.data.AppResult
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
import com.example.avalon_mobile.model.Notificacao
import com.example.avalon_mobile.model.Pergunta
import com.example.avalon_mobile.model.Pesquisa
import com.example.avalon_mobile.model.RespostaAvaliacaoItem
import com.example.avalon_mobile.model.RespostaConfirmacao
import com.example.avalon_mobile.model.RespostaFeedback
import com.example.avalon_mobile.model.RespostaPesquisaItem
import com.example.avalon_mobile.model.SolicitacaoCorrecao
import com.example.avalon_mobile.model.UsuarioResumo
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val usuario: StateFlow<UsuarioResumo?>
    suspend fun login(login: String, senha: String): AppResult<UsuarioResumo>
    suspend fun logout()
    suspend fun esqueciSenha(login: String): AppResult<Unit>
    suspend fun redefinirSenha(token: String, novaSenha: String, confirmacao: String): AppResult<Unit>
}

/** Dados do Colaborador autenticado. O escopo (só os próprios dados) é garantido pelo servidor. */
interface AvalonRepository {
    suspend fun dashboard(): AppResult<Dashboard>
    suspend fun comunicados(): AppResult<List<Comunicado>>

    suspend fun perfil(): AppResult<Colaborador>
    suspend fun solicitarCorrecao(solicitacao: SolicitacaoCorrecao): AppResult<Unit>
    suspend fun jornada(): AppResult<Pair<JornadaEscala, List<DiaSemana>>>

    suspend fun ferias(): AppResult<List<Ferias>>
    suspend fun solicitarFerias(request: FeriasRequest): AppResult<Ferias>

    suspend fun beneficios(): AppResult<List<BeneficioItem>>
    /** Pedido de adesão (aderir=true) ou cancelamento, analisado pelo RH. */
    suspend fun solicitarBeneficio(item: BeneficioItem): AppResult<Unit>

    suspend fun documentos(): AppResult<List<Documento>>
    suspend fun baixarDocumento(documento: Documento): AppResult<ByteArray>

    suspend fun feedbacks(): AppResult<List<Feedback>>
    suspend fun respostasFeedback(idFeedback: Int): AppResult<List<RespostaFeedback>>
    suspend fun responderFeedback(idFeedback: Int, texto: String): AppResult<RespostaFeedback>

    suspend fun pesquisas(): AppResult<List<Pesquisa>>
    suspend fun pesquisa(id: Int): AppResult<Pair<Pesquisa, List<Pergunta>>>
    suspend fun responderPesquisa(id: Int, respostas: List<RespostaPesquisaItem>): AppResult<RespostaConfirmacao>

    suspend fun avaliacaoPendente(): AppResult<Avaliacao?>
    suspend fun perguntasAvaliacao(id: Int): AppResult<List<AvaliacaoPergunta>>
    suspend fun responderAvaliacao(id: Int, respostas: List<RespostaAvaliacaoItem>): AppResult<RespostaConfirmacao>

    suspend fun notificacoes(): AppResult<List<Notificacao>>
}
