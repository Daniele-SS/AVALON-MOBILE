package com.example.avalon_mobile.data.local

import com.example.avalon_mobile.model.TokenResponse
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

/** Fonte única da sessão em memória, sincronizada com o [SecureSessionStore]. */
class SessionManager(private val store: SecureSessionStore) {

    private val _session = MutableStateFlow(store.read())
    val session: StateFlow<Session?> = _session.asStateFlow()

    /** Emitido quando a sessão é perdida sem ação do usuário (refresh negado, conta inativada). */
    private val _expired = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val expired: SharedFlow<Unit> = _expired.asSharedFlow()

    val current: Session? get() = _session.value

    fun start(response: TokenResponse, nowMillis: Long = System.currentTimeMillis()) {
        val session = Session(
            accessToken = response.accessToken,
            refreshToken = response.refreshToken,
            expiresAtMillis = nowMillis + response.expiresIn * 1000,
            usuario = response.usuario
        )
        store.write(session)
        _session.value = session
    }

    fun end() {
        store.clear()
        _session.value = null
    }

    fun expire() {
        if (_session.value != null) {
            end()
            _expired.tryEmit(Unit)
        }
    }

    /** Renova antes de vencer: margem de 60 s sobre o `expires_in` (contrato, item 3 de Autenticação). */
    fun needsRefresh(nowMillis: Long = System.currentTimeMillis()): Boolean =
        current?.let { nowMillis >= it.expiresAtMillis - REFRESH_MARGIN_MS } ?: false

    private companion object {
        const val REFRESH_MARGIN_MS = 60_000L
    }
}
