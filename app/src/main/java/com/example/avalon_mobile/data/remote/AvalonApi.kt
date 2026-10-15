package com.example.avalon_mobile.data.remote

import com.example.avalon_mobile.model.AvaliacaoPergunta
import com.example.avalon_mobile.model.Avaliacao
import com.example.avalon_mobile.model.BeneficioColaborador
import com.example.avalon_mobile.model.Colaborador
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
import com.example.avalon_mobile.model.MeResponse
import com.example.avalon_mobile.model.Notificacao
import com.example.avalon_mobile.model.Pergunta
import com.example.avalon_mobile.model.Pesquisa
import com.example.avalon_mobile.model.RedefinirSenhaRequest
import com.example.avalon_mobile.model.RefreshRequest
import com.example.avalon_mobile.model.RespostaAvaliacaoLote
import com.example.avalon_mobile.model.RespostaConfirmacao
import com.example.avalon_mobile.model.RespostaFeedback
import com.example.avalon_mobile.model.RespostaFeedbackRequest
import com.example.avalon_mobile.model.RespostaPesquisaLote
import com.example.avalon_mobile.model.TokenResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming

/** Rotas consumidas pelo Colaborador, conforme o contrato OpenAPI v2.1.0 (base `/v1/senai/avalon`). */
interface AvalonApi {

    // Autenticação (Sprint 1 e 2 — 6.2.x)
    @POST("v1/senai/avalon/auth/login")
    suspend fun login(@Body body: LoginRequest): Response<TokenResponse>

    @POST("v1/senai/avalon/auth/logout")
    suspend fun logout(@Body body: LogoutRequest): Response<Unit>

    @GET("v1/senai/avalon/auth/me")
    suspend fun me(): Response<MeResponse>

    @POST("v1/senai/avalon/auth/senha/esqueci")
    suspend fun esqueciSenha(@Body body: EsqueciSenhaRequest): Response<Unit>

    @POST("v1/senai/avalon/auth/senha/redefinir")
    suspend fun redefinirSenha(@Body body: RedefinirSenhaRequest): Response<Unit>

    // Dashboard (6.3.1)
    @GET("v1/senai/avalon/dashboard")
    suspend fun dashboard(): Response<Dashboard>

    // Perfil e jornada (6.5.1, 6.5.3)
    @GET("v1/senai/avalon/colaborador/{id}")
    suspend fun colaborador(@Path("id") id: Int): Response<Colaborador>

    @GET("v1/senai/avalon/jornada_escala/{id}")
    suspend fun jornada(@Path("id") id: Int): Response<JornadaEscala>

    @GET("v1/senai/avalon/jornada_escala/{id}/dias")
    suspend fun diasJornada(@Path("id") id: Int): Response<List<DiaSemana>>

    // Férias (6.4)
    @GET("v1/senai/avalon/ferias")
    suspend fun ferias(@Query("id_colaborador") idColaborador: Int): Response<List<Ferias>>

    @POST("v1/senai/avalon/ferias")
    suspend fun solicitarFerias(@Body body: FeriasRequest): Response<Ferias>

    // Benefícios
    @GET("v1/senai/avalon/colaborador/{id}/beneficio")
    suspend fun beneficiosDoColaborador(@Path("id") id: Int): Response<List<BeneficioColaborador>>

    // Documentos (6.5.4)
    @GET("v1/senai/avalon/documento")
    suspend fun documentos(@Query("id_colaborador") idColaborador: Int, @Query("ativo") ativo: Boolean = true): Response<List<Documento>>

    @Streaming
    @GET("v1/senai/avalon/documento/{id}/arquivo")
    suspend fun baixarDocumento(@Path("id") id: Int): Response<ResponseBody>

    // Feedback (6.7)
    @GET("v1/senai/avalon/feedback")
    suspend fun feedbacks(@Query("id_colaborador_destinatario") idDestinatario: Int): Response<List<Feedback>>

    @GET("v1/senai/avalon/feedback/{id}/resposta")
    suspend fun respostasFeedback(@Path("id") id: Int): Response<List<RespostaFeedback>>

    @POST("v1/senai/avalon/feedback/{id}/resposta")
    suspend fun responderFeedback(@Path("id") id: Int, @Body body: RespostaFeedbackRequest): Response<RespostaFeedback>

    // Pesquisas (6.6)
    @GET("v1/senai/avalon/pesquisa")
    suspend fun pesquisas(@Query("somente_pendentes") somentePendentes: Boolean? = null): Response<List<Pesquisa>>

    @GET("v1/senai/avalon/pesquisa/{id}")
    suspend fun pesquisa(@Path("id") id: Int): Response<Pesquisa>

    @GET("v1/senai/avalon/pesquisa/{id}/pergunta")
    suspend fun perguntasPesquisa(@Path("id") id: Int): Response<List<Pergunta>>

    @POST("v1/senai/avalon/pesquisa/{id}/resposta")
    suspend fun responderPesquisa(@Path("id") id: Int, @Body body: RespostaPesquisaLote): Response<RespostaConfirmacao>

    // Avaliação de fatores psicossociais (6.6 / NR-1)
    @GET("v1/senai/avalon/avaliacao")
    suspend fun avaliacoes(@Query("somente_pendentes") somentePendentes: Boolean = true): Response<List<Avaliacao>>

    @GET("v1/senai/avalon/avaliacao/{id}/pergunta")
    suspend fun perguntasAvaliacao(@Path("id") id: Int): Response<List<AvaliacaoPergunta>>

    @POST("v1/senai/avalon/avaliacao/{id}/resposta")
    suspend fun responderAvaliacao(@Path("id") id: Int, @Body body: RespostaAvaliacaoLote): Response<RespostaConfirmacao>

    // Notificações (6.3.4)
    @GET("v1/senai/avalon/notificacao")
    suspend fun notificacoes(): Response<List<Notificacao>>
}

/** Cliente separado e sem autenticador, usado apenas para renovar o token (evita recursão no 401). */
interface AuthRefreshApi {
    @POST("v1/senai/avalon/auth/refresh")
    fun refresh(@Body body: RefreshRequest): retrofit2.Call<TokenResponse>
}
