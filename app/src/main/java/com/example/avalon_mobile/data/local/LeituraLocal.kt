package com.example.avalon_mobile.data.local

import android.content.SharedPreferences

/**
 * Estado de leitura guardado no aparelho. O banco não tem o campo "lido" para feedback nem para notificação,
 * então o app lembra, por colaborador, quais ids já foram abertos. Guarda só ids, sem conteúdo.
 */
interface LeituraLocal {
    fun ids(tipo: String, idColaborador: Int): Set<Int>
    fun marcar(tipo: String, idColaborador: Int, ids: Collection<Int>)

    companion object {
        const val FEEDBACK = "feedback"
        const val NOTIFICACAO = "notificacao"
        const val ARQUIVO = "avalon_leitura"
    }
}

class LeituraLocalPrefs(private val prefs: SharedPreferences) : LeituraLocal {

    private fun chave(tipo: String, idColaborador: Int) = "${tipo}_$idColaborador"

    override fun ids(tipo: String, idColaborador: Int): Set<Int> =
        prefs.getStringSet(chave(tipo, idColaborador), emptySet()).orEmpty().mapNotNull { it.toIntOrNull() }.toSet()

    override fun marcar(tipo: String, idColaborador: Int, ids: Collection<Int>) {
        if (ids.isEmpty()) return
        val novos = ids(tipo, idColaborador) + ids
        prefs.edit().putStringSet(chave(tipo, idColaborador), novos.map { it.toString() }.toSet()).apply()
    }
}
