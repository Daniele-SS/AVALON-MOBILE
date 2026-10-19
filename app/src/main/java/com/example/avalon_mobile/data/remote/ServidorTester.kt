package com.example.avalon_mobile.data.remote

import com.example.avalon_mobile.model.ApiErro
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

sealed interface ResultadoTeste {
    data object Avalon : ResultadoTeste
    data class OutroServico(val status: Int) : ResultadoTeste
    data object SemResposta : ResultadoTeste
}

class ServidorTester(private val client: OkHttpClient, private val gson: Gson) {

    suspend fun testar(base: HttpUrl): ResultadoTeste = withContext(Dispatchers.IO) {
        val url = base.newBuilder().addPathSegments("v1/senai/avalon/auth/me").build()
        try {
            client.newCall(Request.Builder().url(url).get().build()).execute().use { resposta ->
                val corpo = resposta.body?.string().orEmpty()
                val erro = runCatching { gson.fromJson(corpo, ApiErro::class.java) }.getOrNull()
                if (resposta.code == 401 && erro?.codigo != null) ResultadoTeste.Avalon
                else ResultadoTeste.OutroServico(resposta.code)
            }
        } catch (e: IOException) {
            ResultadoTeste.SemResposta
        }
    }
}
