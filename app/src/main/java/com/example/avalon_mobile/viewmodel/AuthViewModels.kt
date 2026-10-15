package com.example.avalon_mobile.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.avalon_mobile.data.AppResult
import com.example.avalon_mobile.data.repository.AuthRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val login: String = "",
    val senha: String = "",
    val loginError: String? = null,
    val senhaError: String? = null,
    /** Erro geral (credenciais inválidas, sem conexão). */
    val error: String? = null,
    /** 6.2.1: conta inativa tem tratamento próprio (orienta procurar o RH). */
    val contaInativa: Boolean = false,
    val loading: Boolean = false,
    val enviandoRecuperacao: Boolean = false
)

sealed interface LoginEvent {
    data object Autenticado : LoginEvent
    data class CodigoEnviado(val login: String) : LoginEvent
}

class LoginViewModel(private val auth: AuthRepository) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    private val _events = Channel<LoginEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onLoginChange(value: String) = _state.update { it.copy(login = value, loginError = null, error = null, contaInativa = false) }
    fun onSenhaChange(value: String) = _state.update { it.copy(senha = value, senhaError = null, error = null, contaInativa = false) }

    fun entrar() {
        val s = _state.value
        val loginError = if (s.login.isBlank()) "Informe seu login." else null
        val senhaError = if (s.senha.isEmpty()) "Informe sua senha." else null
        if (loginError != null || senhaError != null) {
            _state.update { it.copy(loginError = loginError, senhaError = senhaError) }
            return
        }
        _state.update { it.copy(loading = true, error = null, contaInativa = false) }
        viewModelScope.launch {
            when (val result = auth.login(s.login, s.senha)) {
                is AppResult.Success -> {
                    _state.update { it.copy(loading = false, senha = "") }
                    _events.send(LoginEvent.Autenticado)
                }
                is AppResult.Failure -> _state.update {
                    it.copy(
                        loading = false,
                        senha = "",
                        contaInativa = result.error.codigo == "CONTA_INATIVA",
                        error = result.error.mensagem
                    )
                }
            }
        }
    }

    /** Recuperação de senha (6.2.2): o contrato envia o código ao e-mail cadastrado do login informado. */
    fun esqueciSenha() {
        val login = _state.value.login.trim()
        if (login.isEmpty()) {
            _state.update { it.copy(loginError = "Informe seu login para recuperar a senha.") }
            return
        }
        _state.update { it.copy(enviandoRecuperacao = true, error = null) }
        viewModelScope.launch {
            val result = auth.esqueciSenha(login)
            _state.update { it.copy(enviandoRecuperacao = false) }
            when (result) {
                is AppResult.Success -> _events.send(LoginEvent.CodigoEnviado(login))
                is AppResult.Failure -> _state.update { it.copy(error = result.error.mensagem) }
            }
        }
    }
}

data class RedefinirSenhaUiState(
    val novaSenha: String = "",
    val confirmacao: String = "",
    val novaSenhaError: String? = null,
    val confirmacaoError: String? = null,
    val error: String? = null,
    val loading: Boolean = false,
    val concluido: Boolean = false
)

class RedefinirSenhaViewModel(
    private val auth: AuthRepository,
    handle: SavedStateHandle
) : ViewModel() {

    private val token: String = checkNotNull(handle["token"])

    private val _state = MutableStateFlow(RedefinirSenhaUiState())
    val state: StateFlow<RedefinirSenhaUiState> = _state.asStateFlow()

    fun onNovaSenha(v: String) = _state.update { it.copy(novaSenha = v, novaSenhaError = null, error = null) }
    fun onConfirmacao(v: String) = _state.update { it.copy(confirmacao = v, confirmacaoError = null, error = null) }

    fun confirmar() {
        val s = _state.value
        val politica = validarPolitica(s.novaSenha)
        val confirmacao = if (s.confirmacao != s.novaSenha) "As senhas não conferem." else null
        if (politica != null || confirmacao != null) {
            _state.update { it.copy(novaSenhaError = politica, confirmacaoError = confirmacao) }
            return
        }
        _state.update { it.copy(loading = true) }
        viewModelScope.launch {
            when (val r = auth.redefinirSenha(token, s.novaSenha, s.confirmacao)) {
                is AppResult.Success -> _state.update { it.copy(loading = false, concluido = true) }
                is AppResult.Failure -> _state.update { it.copy(loading = false, error = r.error.mensagem) }
            }
        }
    }

    companion object {
        /**
         * Pré-validação (RF 5.1.41). A política oficial é configurável e aplicada pelo Back-end;
         * aqui só se evita uma ida ao servidor com senha obviamente fraca.
         */
        fun validarPolitica(senha: String): String? = when {
            senha.length < 8 -> "Use pelo menos 8 caracteres."
            !senha.any(Char::isLetter) || !senha.any(Char::isDigit) -> "Combine letras e números."
            else -> null
        }
    }
}
