package com.example.avalon_mobile.data

/** Resultado de operações de dados: sucesso ou falha já traduzida para o usuário. */
sealed interface AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>
    data class Failure(val error: AppError) : AppResult<Nothing>
}

/**
 * Erro de domínio. [codigo] segue a tabela de erros do contrato (ex.: CONTA_INATIVA);
 * [mensagem] já está pronta para exibição.
 */
data class AppError(
    val codigo: String,
    val mensagem: String,
    val httpStatus: Int? = null
) {
    companion object {
        const val SEM_CONEXAO = "SEM_CONEXAO"
        const val NAO_IMPLEMENTADO = "NAO_IMPLEMENTADO"
        const val DESCONHECIDO = "ERRO_INTERNO"
    }
}

inline fun <T, R> AppResult<T>.map(transform: (T) -> R): AppResult<R> = when (this) {
    is AppResult.Success -> AppResult.Success(transform(data))
    is AppResult.Failure -> this
}
