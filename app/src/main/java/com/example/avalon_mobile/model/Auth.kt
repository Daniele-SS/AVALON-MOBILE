package com.example.avalon_mobile.model

import com.google.gson.annotations.SerializedName

// Modelos de autenticação conforme o contrato OpenAPI (openapi.yaml, tag AUTENTICAÇÃO).

data class LoginRequest(
    val login: String,
    val senha: String
)

data class RefreshRequest(
    @SerializedName("refresh_token") val refreshToken: String
)

data class LogoutRequest(
    @SerializedName("refresh_token") val refreshToken: String?
)

data class EsqueciSenhaRequest(
    val login: String
)

data class RedefinirSenhaRequest(
    val token: String,
    @SerializedName("nova_senha") val novaSenha: String,
    @SerializedName("confirmacao_nova_senha") val confirmacaoNovaSenha: String
)

data class AlterarSenhaRequest(
    @SerializedName("senha_atual") val senhaAtual: String,
    @SerializedName("nova_senha") val novaSenha: String,
    @SerializedName("confirmacao_nova_senha") val confirmacaoNovaSenha: String
)

data class TokenResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String,
    @SerializedName("expires_in") val expiresIn: Long,
    @SerializedName("refresh_token") val refreshToken: String,
    val usuario: UsuarioResumo,
    val menus: List<Menu> = emptyList()
)

data class UsuarioResumo(
    val id: Int,
    val login: String,
    @SerializedName("id_colaborador") val idColaborador: Int,
    val nome: String,
    @SerializedName("nivel_acesso") val nivelAcesso: NivelAcesso
)

data class NivelAcesso(
    val id: Int,
    val nome: String,
    val status: Boolean = true
) {
    companion object {
        // RN 12.1.2: perfis fixos. O aplicativo mobile é o canal do Colaborador.
        const val COLABORADOR = 1
        const val GESTOR = 2
        const val RH = 3
    }
}

data class Menu(
    val id: Int,
    val nome: String,
    val icone: String? = null,
    val rota: String,
    val ordem: Int
)

data class MeResponse(
    val usuario: UsuarioResumo,
    val menus: List<Menu> = emptyList()
)

/** Envelope único de erro (componente `Erro`). O cliente decide pelo [codigo]. */
data class ApiErro(
    val codigo: String? = null,
    val mensagem: String? = null,
    val detalhes: List<ErroDetalhe>? = null
)

data class ErroDetalhe(
    val campo: String,
    val problema: String
)
