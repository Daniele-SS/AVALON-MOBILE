package com.example.avalon_mobile.data.remote

import com.example.avalon_mobile.model.AlterarSenhaRequest
import com.example.avalon_mobile.model.SolicitarCodigoSenhaRequest
import com.google.gson.JsonElement
import com.example.avalon_mobile.model.AvaliacaoPergunta
import com.example.avalon_mobile.model.Avaliacao
import com.example.avalon_mobile.model.BeneficioColaborador
import com.example.avalon_mobile.model.Dashboard
import com.example.avalon_mobile.model.Documento
import com.example.avalon_mobile.model.Ferias
import com.example.avalon_mobile.model.FeriasRequest
import com.example.avalon_mobile.model.LoginRequest
import com.example.avalon_mobile.model.LogoutRequest
import com.example.avalon_mobile.model.MeResponse
import com.example.avalon_mobile.model.RedefinirRecuperacaoRequest
import com.example.avalon_mobile.model.SolicitarRecuperacaoRequest
import com.example.avalon_mobile.model.RefreshRequest
import com.example.avalon_mobile.model.RespostaAvaliacaoLote
import com.example.avalon_mobile.model.RespostaPesquisaLote
import com.example.avalon_mobile.model.RespostaConfirmacao
import com.example.avalon_mobile.model.TokenResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
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

    @POST("v1/senai/avalon/auth/senha/codigo")
    suspend fun solicitarCodigoSenha(@Body body: SolicitarCodigoSenhaRequest): Response<Unit>

    @PUT("v1/senai/avalon/auth/senha")
    suspend fun alterarSenha(@Body body: AlterarSenhaRequest): Response<Unit>

    // Recuperação de senha (rotas públicas publicadas em 06/11/2026): código de 4 dígitos enviado ao e-mail.
    @Headers("Content-Type: application/json")
    @POST("v1/senai/avalon/recuperacao_senha/solicitar")
    suspend fun solicitarRecuperacao(@Body body: SolicitarRecuperacaoRequest): Response<Unit>

    @Headers("Content-Type: application/json")
    @POST("v1/senai/avalon/recuperacao_senha/redefinir")
    suspend fun redefinirRecuperacao(@Body body: RedefinirRecuperacaoRequest): Response<Unit>

    // Dashboard (6.3.1)
    @GET("v1/senai/avalon/dashboard")
    suspend fun dashboard(): Response<Dashboard>

    // Perfil e jornada (6.5.1, 6.5.3)
    @GET("v1/senai/avalon/colaborador/{id}")
    suspend fun colaborador(@Path("id") id: Int): Response<JsonElement>

    @GET("v1/senai/avalon/jornada_escala/{id}")
    suspend fun jornada(@Path("id") id: Int): Response<JsonElement>

    @GET("v1/senai/avalon/jornada_escala/{id}/dias")
    suspend fun diasJornada(@Path("id") id: Int): Response<JsonElement>

    @GET("v1/senai/avalon/dia_semana")
    suspend fun diasSemana(): Response<JsonElement>

    // Férias (6.4)
    @GET("v1/senai/avalon/ferias")
    suspend fun ferias(): Response<JsonElement>

    // O Back-end compara o Content-Type com "application/json" exato; sem isto o Retrofit acrescenta "; charset=UTF-8" e a API responde 415.
    @Headers("Content-Type: application/json")
    @POST("v1/senai/avalon/ferias")
    suspend fun solicitarFerias(@Body body: FeriasRequest): Response<JsonElement>

    // Benefícios
    @GET("v1/senai/avalon/beneficio_colaborador")
    suspend fun concessoesBeneficio(): Response<JsonElement>

    @GET("v1/senai/avalon/beneficio")
    suspend fun catalogoBeneficio(): Response<JsonElement>

    // Documentos (6.5.4)
    @GET("v1/senai/avalon/documento")
    suspend fun documentos(): Response<JsonElement>

    // Devolve o arquivo (attachment). O Back-end publicou esta rota em 09/11/2026.
    @Streaming
    @GET("v1/senai/avalon/documento/download/{id}")
    suspend fun baixarDocumento(@Path("id") id: Int): Response<ResponseBody>

    // Feedback (6.7)
    // Publicado em 06/11/2026: lista de todos os colaboradores (sem escopo por perfil); o app filtra pelo titular.
    @GET("v1/senai/avalon/feedback")
    suspend fun feedbacks(): Response<JsonElement>

    // Resposta do destinatário ao feedback (Back-end, 12/11/2026). A listagem traz as de todos; o app filtra por feedback e titular.
    @GET("v1/senai/avalon/resposta_feedback")
    suspend fun respostasFeedback(): Response<JsonElement>

    // Só o perfil Colaborador grava; uma resposta por feedback (a duplicidade volta como HTTP 500 com corpo HTML).
    @Headers("Content-Type: application/json")
    @POST("v1/senai/avalon/resposta_feedback")
    suspend fun responderFeedback(@Body body: RespostaFeedbackNova): Response<JsonElement>

    // Pesquisas (6.6)
    // Rotas publicadas pelo Back-end (05/11/2026): listagens completas; o app filtra pelas participações do titular.
    @GET("v1/senai/avalon/pesquisa")
    suspend fun listaPesquisas(): Response<JsonElement>

    // Publicado em 11/11/2026: envio em lote; a participação do titular é resolvida pelo servidor pelo token.
    @Headers("Content-Type: application/json")
    @POST("v1/senai/avalon/pesquisa/{id}/resposta")
    suspend fun responderPesquisa(@Path("id") id: Int, @Body body: RespostaPesquisaLote): Response<JsonElement>

    @GET("v1/senai/avalon/pergunta")
    suspend fun listaPerguntas(): Response<JsonElement>

    @GET("v1/senai/avalon/participacao")
    suspend fun participacoes(): Response<JsonElement>

    // Avaliação de fatores psicossociais (6.6 / NR-1)
    @GET("v1/senai/avalon/avaliacao")
    suspend fun avaliacoes(@Query("somente_pendentes") somentePendentes: Boolean = true): Response<List<Avaliacao>>

    @GET("v1/senai/avalon/avaliacao/{id}/pergunta")
    suspend fun perguntasAvaliacao(@Path("id") id: Int): Response<List<AvaliacaoPergunta>>

    @POST("v1/senai/avalon/avaliacao/{id}/resposta")
    suspend fun responderAvaliacao(@Path("id") id: Int, @Body body: RespostaAvaliacaoLote): Response<RespostaConfirmacao>

    // Notificações (6.3.4)
    // Publicado em 06/11/2026: lista de todos os colaboradores; o app filtra pelo titular.
    @GET("v1/senai/avalon/notificacao")
    suspend fun notificacoes(): Response<JsonElement>
}

/** Cliente separado e sem autenticador, usado apenas para renovar o token (evita recursão no 401). */
interface AuthRefreshApi {
    @POST("v1/senai/avalon/auth/refresh")
    fun refresh(@Body body: RefreshRequest): retrofit2.Call<TokenResponse>
}
