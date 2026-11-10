package com.example.avalon_mobile.model

/**
 * Solicitação de correção cadastral registrada no aparelho (histórico "Minhas solicitações").
 * O status é local: enquanto o back-end não expõe a rota de correção, o app só sabe que enviou ("EM_ANALISE").
 */
data class CorrecaoRegistrada(
    val id: Long,
    val campo: String,
    val valorSugerido: String,
    val justificativa: String,
    val criadaEm: String,
    val status: String = EM_ANALISE
) {
    companion object {
        const val EM_ANALISE = "EM_ANALISE"
    }
}
