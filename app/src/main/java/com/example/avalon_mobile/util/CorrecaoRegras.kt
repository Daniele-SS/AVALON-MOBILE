package com.example.avalon_mobile.util

import com.example.avalon_mobile.model.CorrecaoRegistrada

/** Regras de cliente da correção cadastral (6.5.2). Sem Android: testável na JVM. A decisão final é do RH. */
object CorrecaoRegras {
    val CAMPOS = listOf("Nome", "CPF", "E-mail", "Cargo", "Setor", "Matrícula")
    const val JUSTIFICATIVA_MIN = 10
    const val JUSTIFICATIVA_MAX = 500
    const val HISTORICO_MAX = 20

    private val EMAIL = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$")

    /** Mensagem de erro, ou null se válido. */
    fun validar(campo: String, valor: String, justificativa: String): String? {
        val v = valor.trim()
        val j = justificativa.trim()
        if (campo !in CAMPOS) return "Escolha o campo a corrigir."
        if (v.isEmpty()) return "Informe o valor correto."
        erroDoValor(campo, v)?.let { return it }
        if (j.length < JUSTIFICATIVA_MIN) return "Explique o motivo em pelo menos $JUSTIFICATIVA_MIN caracteres."
        if (j.length > JUSTIFICATIVA_MAX) return "A justificativa pode ter no máximo $JUSTIFICATIVA_MAX caracteres."
        return null
    }

    private fun erroDoValor(campo: String, v: String): String? = when (campo) {
        "Nome" -> if (v.length < 3 || v.none { it.isLetter() }) "Informe o nome completo." else null
        "CPF" -> if (!cpfValido(v)) "CPF inválido. Confira os 11 dígitos." else null
        "E-mail" -> if (!EMAIL.matches(v)) "Informe um e-mail válido." else null
        "Matrícula" -> if (v.length < 3 || !v.all { it.isLetterOrDigit() || it == '-' }) "Matrícula inválida." else null
        else -> if (v.length < 2) "Informe o valor correto." else null
    }

    fun cpfValido(texto: String): Boolean {
        val d = texto.filter(Char::isDigit)
        if (d.length != 11 || d.all { it == d[0] }) return false
        fun digito(qtd: Int): Int {
            val soma = (0 until qtd).sumOf { (d[it] - '0') * (qtd + 1 - it) }
            val resto = (soma * 10) % 11
            return if (resto == 10) 0 else resto
        }
        return digito(9) == d[9] - '0' && digito(10) == d[10] - '0'
    }

    /** Já existe pedido em análise para o campo? Evita duplicar enquanto o RH não responde. */
    fun pendentePara(campo: String, historico: List<CorrecaoRegistrada>): Boolean =
        historico.any { it.campo == campo && it.status == CorrecaoRegistrada.EM_ANALISE }

    /** Mais recente primeiro, limitado a [HISTORICO_MAX]. */
    fun adicionar(historico: List<CorrecaoRegistrada>, nova: CorrecaoRegistrada): List<CorrecaoRegistrada> =
        (listOf(nova) + historico).take(HISTORICO_MAX)
}
