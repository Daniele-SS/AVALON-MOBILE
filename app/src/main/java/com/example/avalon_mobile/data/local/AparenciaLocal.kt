package com.example.avalon_mobile.data.local

import android.content.SharedPreferences

/** Modo de tema escolhido pelo usuário. SISTEMA segue a configuração do aparelho. */
enum class TemaApp(val chave: String, val rotulo: String) {
    SISTEMA("sistema", "Seguir o aparelho"),
    CLARO("claro", "Claro"),
    ESCURO("escuro", "Escuro");

    companion object {
        fun deChave(chave: String?): TemaApp = values().firstOrNull { it.chave == chave } ?: SISTEMA
    }
}

/** Preferências de aparência e acessibilidade (RNF de acessibilidade, WCAG 2.1 AA). Ficam só no aparelho. */
data class Aparencia(
    val tema: TemaApp = TemaApp.SISTEMA,
    val altoContraste: Boolean = false,
    val escalaFonte: Float = EscalasFonte.PADRAO
)

/** Escalas de fonte oferecidas. Valores fora da lista (arquivo adulterado) caem na mais próxima. */
object EscalasFonte {
    const val PADRAO = 1.0f
    val opcoes: List<Pair<Float, String>> = listOf(
        0.85f to "Pequena",
        PADRAO to "Padrão",
        1.15f to "Grande",
        1.3f to "Muito grande"
    )

    fun normalizar(valor: Float?): Float {
        if (valor == null || valor.isNaN()) return PADRAO
        return opcoes.minByOrNull { kotlin.math.abs(it.first - valor) }!!.first
    }

    fun rotulo(valor: Float): String = opcoes.firstOrNull { it.first == normalizar(valor) }?.second ?: "Padrão"
}

interface AparenciaLocal {
    fun ler(): Aparencia
    fun salvar(aparencia: Aparencia)

    companion object {
        const val ARQUIVO = "avalon_aparencia"
    }
}

class AparenciaLocalPrefs(private val prefs: SharedPreferences) : AparenciaLocal {

    override fun ler(): Aparencia = Aparencia(
        tema = TemaApp.deChave(prefs.getString(CH_TEMA, null)),
        altoContraste = prefs.getBoolean(CH_CONTRASTE, false),
        escalaFonte = EscalasFonte.normalizar(prefs.getFloat(CH_ESCALA, EscalasFonte.PADRAO))
    )

    override fun salvar(aparencia: Aparencia) {
        prefs.edit()
            .putString(CH_TEMA, aparencia.tema.chave)
            .putBoolean(CH_CONTRASTE, aparencia.altoContraste)
            .putFloat(CH_ESCALA, EscalasFonte.normalizar(aparencia.escalaFonte))
            .apply()
    }

    private companion object {
        const val CH_TEMA = "tema"
        const val CH_CONTRASTE = "alto_contraste"
        const val CH_ESCALA = "escala_fonte"
    }
}
