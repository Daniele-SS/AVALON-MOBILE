package com.example.avalon_mobile.data.remote

import com.example.avalon_mobile.data.local.SessionManager
import com.example.avalon_mobile.model.RefreshRequest
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

/**
 * Renovação do access token com rotação do refresh token (6.2.3).
 * Sincronizado: requisições concorrentes que expiram juntas disparam um único refresh,
 * pois reapresentar um refresh já consumido revoga a sessão inteira no servidor.
 */
class TokenRefresher(
    private val sessionManager: SessionManager,
    private val refreshApi: AuthRefreshApi
) {
    /** Devolve o access token válido após garantir a renovação, ou null se a sessão caiu. */
    @Synchronized
    fun refresh(staleAccessToken: String?): String? {
        val session = sessionManager.current ?: return null
        // Outra thread já renovou enquanto esta aguardava o lock.
        if (staleAccessToken != null && session.accessToken != staleAccessToken && !sessionManager.needsRefresh()) {
            return session.accessToken
        }
        val response = runCatching { refreshApi.refresh(RefreshRequest(session.refreshToken)).execute() }
            .getOrNull()
            ?: return null // sem rede: mantém a sessão; a chamada original falhará com erro de conexão
        val body = response.body()
        return if (response.isSuccessful && body != null) {
            sessionManager.start(body)
            body.accessToken
        } else {
            if (response.code() == 401 || response.code() == 403) sessionManager.expire()
            null
        }
    }
}

/** Anexa `Authorization: Bearer` e renova proativamente quando o token está para vencer. */
class AuthInterceptor(
    private val sessionManager: SessionManager,
    private val refresher: TokenRefresher
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (request.header(NO_AUTH_HEADER) != null) {
            return chain.proceed(request.newBuilder().removeHeader(NO_AUTH_HEADER).build())
        }
        val token = if (sessionManager.needsRefresh()) {
            refresher.refresh(sessionManager.current?.accessToken) ?: sessionManager.current?.accessToken
        } else {
            sessionManager.current?.accessToken
        }
        return chain.proceed(request.withBearer(token))
    }

    companion object {
        const val NO_AUTH_HEADER = "X-Avalon-No-Auth"
    }
}

/** Em 401 tenta uma única renovação e repete a requisição; se falhar, encerra a sessão. */
class TokenAuthenticator(
    private val sessionManager: SessionManager,
    private val refresher: TokenRefresher
) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.request.header("Authorization") == null) return null
        if (response.priorResponse != null) {
            sessionManager.expire()
            return null
        }
        val stale = response.request.header("Authorization")?.removePrefix("Bearer ")
        val fresh = refresher.refresh(stale) ?: run {
            sessionManager.expire()
            return null
        }
        return response.request.withBearer(fresh)
    }
}

private fun Request.withBearer(token: String?): Request =
    if (token == null) this else newBuilder().header("Authorization", "Bearer $token").build()
