package com.example.avalon_mobile.data.local

import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

class ApiConfig(private val prefs: SharedPreferences, padrao: String) {

    val padrao: HttpUrl = padrao.toHttpUrl()

    private val _baseUrl = MutableStateFlow(prefs.getString(CHAVE, null)?.let(::normalizar) ?: this.padrao)
    val baseUrl: StateFlow<HttpUrl> = _baseUrl.asStateFlow()

    val atual: HttpUrl get() = _baseUrl.value

    fun salvar(url: HttpUrl) {
        prefs.edit().putString(CHAVE, url.toString()).apply()
        _baseUrl.value = url
    }

    fun restaurarPadrao() {
        prefs.edit().remove(CHAVE).apply()
        _baseUrl.value = padrao
    }

    companion object {
        const val ARQUIVO = "avalon_api"
        private const val CHAVE = "base_url"

        fun normalizar(texto: String): HttpUrl? {
            val limpo = texto.trim()
            if (limpo.isEmpty()) return null
            val comEsquema = if ("://" in limpo) limpo else "http://$limpo"
            val url = comEsquema.toHttpUrlOrNull() ?: return null
            if (url.host.isBlank() || url.encodedPath != "/" || url.query != null) return null
            return HttpUrl.Builder().scheme(url.scheme).host(url.host).port(url.port).build()
        }
    }
}
