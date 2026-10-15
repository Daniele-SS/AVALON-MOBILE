package com.example.avalon_mobile.data.remote

import com.example.avalon_mobile.data.AppError
import com.example.avalon_mobile.data.AppResult
import com.example.avalon_mobile.model.ApiErro
import com.google.gson.Gson
import retrofit2.Response
import java.io.IOException

/** Converte a resposta HTTP no [AppResult], usando o `codigo` do envelope `Erro` do contrato. */
class ApiCall(private val gson: Gson) {

    suspend fun <T> execute(block: suspend () -> Response<T>): AppResult<T> = try {
        val response = block()
        if (response.isSuccessful) {
            @Suppress("UNCHECKED_CAST")
            AppResult.Success(response.body() ?: Unit as T)
        } else {
            AppResult.Failure(parseError(response))
        }
    } catch (e: IOException) {
        AppResult.Failure(AppError(AppError.SEM_CONEXAO, "Não foi possível conectar ao servidor. Verifique sua conexão."))
    } catch (e: RuntimeException) {
        AppResult.Failure(AppError(AppError.DESCONHECIDO, "Resposta inesperada do servidor."))
    }

    private fun parseError(response: Response<*>): AppError {
        val erro = runCatching { gson.fromJson(response.errorBody()?.charStream(), ApiErro::class.java) }.getOrNull()
        val codigo = erro?.codigo ?: when (response.code()) {
            501 -> AppError.NAO_IMPLEMENTADO
            else -> "HTTP_${response.code()}"
        }
        return AppError(codigo, mensagemPara(codigo, erro?.mensagem), response.code())
    }

    private fun mensagemPara(codigo: String, mensagemServidor: String?): String = when (codigo) {
        "CREDENCIAIS_INVALIDAS" -> "Login ou senha inválidos."
        "CONTA_INATIVA" -> "Esta conta está inativa. Procure o RH."
        "TOKEN_INVALIDO" -> "Sua sessão expirou. Faça login novamente."
        "MUITAS_TENTATIVAS" -> "Muitas tentativas. Aguarde alguns minutos e tente de novo."
        "TOKEN_REDEFINICAO_INVALIDO" -> "O código expirou ou já foi utilizado."
        "FERIAS_SOBREPOSTAS" -> "Já existe um pedido de férias nesse período."
        "PESQUISA_JA_RESPONDIDA", "AVALIACAO_JA_RESPONDIDA" -> "Você já respondeu este questionário."
        "RESPOSTA_OBRIGATORIA_AUSENTE" -> "Responda todas as perguntas obrigatórias."
        AppError.NAO_IMPLEMENTADO -> "Funcionalidade ainda não disponível no servidor."
        else -> mensagemServidor ?: "Não foi possível concluir a operação."
    }
}
