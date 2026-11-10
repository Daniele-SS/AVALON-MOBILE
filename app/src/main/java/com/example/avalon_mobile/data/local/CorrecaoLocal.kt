package com.example.avalon_mobile.data.local

import android.content.SharedPreferences
import com.example.avalon_mobile.model.CorrecaoRegistrada
import com.example.avalon_mobile.util.CorrecaoRegras
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/** Histórico de correções cadastrais pedidas neste aparelho, por colaborador. Guarda o que o próprio usuário digitou. */
interface CorrecaoLocal {
    fun listar(idColaborador: Int): List<CorrecaoRegistrada>
    fun registrar(idColaborador: Int, correcao: CorrecaoRegistrada)

    companion object {
        const val ARQUIVO = "avalon_correcoes"
    }
}

class CorrecaoLocalPrefs(private val prefs: SharedPreferences, private val gson: Gson = Gson()) : CorrecaoLocal {

    private fun chave(id: Int) = "correcoes_$id"

    override fun listar(idColaborador: Int): List<CorrecaoRegistrada> = desserializar(prefs.getString(chave(idColaborador), null), gson)

    override fun registrar(idColaborador: Int, correcao: CorrecaoRegistrada) {
        val novo = CorrecaoRegras.adicionar(listar(idColaborador), correcao)
        prefs.edit().putString(chave(idColaborador), gson.toJson(novo)).apply()
    }

    companion object {
        /** JSON ausente ou corrompido vira lista vazia, nunca exceção. */
        fun desserializar(json: String?, gson: Gson = Gson()): List<CorrecaoRegistrada> {
            if (json.isNullOrBlank()) return emptyList()
            return runCatching {
                gson.fromJson<List<CorrecaoRegistrada>>(json, object : TypeToken<List<CorrecaoRegistrada>>() {}.type)
            }.getOrNull().orEmpty().filterNotNull()
        }
    }
}
