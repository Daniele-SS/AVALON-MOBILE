package com.example.avalon_mobile.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.avalon_mobile.data.AppResult
import com.example.avalon_mobile.data.repository.AuthRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
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
    /** Conta bloqueada após 4 erros do código de redefinição: só o RH redefine a senha (RF 5.1.85/5.1.86). */
    val contaBloqueada: Boolean = false,
    val loading: Boolean = false,
    val enviandoRecuperacao: Boolean = false
)

sealed interface LoginEvent {
    data object Autenticado : LoginEvent
    data class CodigoEnviado(val login: String) : LoginEvent
}

/** Códigos de erro que indicam conta bloqueada por excesso de erros do código (nome a confirmar com o Back-end). */
val CODIGOS_CONTA_BLOQUEADA = setOf("CONTA_BLOQUEADA", "TENTATIVAS_EXCEDIDAS")

private val EMAIL = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

/** RF 5.1.34: o Colaborador entra com o e-mail corporativo. O servidor decide se a conta existe. */
fun validarEmailCorporativo(valor: String, recuperacao: Boolean = false): String? = when {
    valor.isBlank() ->
        if (recuperacao) "Informe seu e-mail corporativo para recuperar a senha." else "Informe seu e-mail corporativo."
    !EMAIL.matches(valor.trim()) -> "Informe um e-mail válido."
    else -> null
}

class LoginViewModel(private val auth: AuthRepository) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    private val _events = Channel<LoginEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onLoginChange(value: String) = _state.update { it.copy(login = value, loginError = null, error = null, contaInativa = false, contaBloqueada = false) }
    fun onSenhaChange(value: String) = _state.update { it.copy(senha = value, senhaError = null, error = null, contaInativa = false, contaBloqueada = false) }

    fun entrar() {
        val s = _state.value
        val loginError = validarEmailCorporativo(s.login)
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
                        contaBloqueada = result.error.codigo in CODIGOS_CONTA_BLOQUEADA,
                        error = result.error.mensagem
                    )
                }
            }
        }
    }

    /** Recuperação de senha (6.2.2): o código de 4 dígitos é enviado ao e-mail corporativo informado. */
    fun esqueciSenha() {
        val login = _state.value.login.trim()
        val loginError = validarEmailCorporativo(login, recuperacao = true)
        if (loginError != null) {
            _state.update { it.copy(loginError = loginError) }
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
    /** 4º erro do código: conta bloqueada, só o RH redefine a senha. */
    val contaBloqueada: Boolean = false,
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
                is AppResult.Failure -> _state.update {
                    it.copy(
                        loading = false,
                        error = r.error.mensagem,
                        contaBloqueada = r.error.codigo in CODIGOS_CONTA_BLOQUEADA
                    )
                }
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

data class CodigoRecuperacaoUiState(
    val codigo: String = "",
    /** Segundos até liberar o reenvio (2 minutos após cada envio, RF 5.1.84). */
    val segundosParaReenviar: Int = SEGUNDOS_REENVIO,
    val reenviando: Boolean = false,
    val mensagem: String? = null,
    val erro: String? = null,
    val contaBloqueada: Boolean = false
)

const val TAMANHO_CODIGO = 4
const val SEGUNDOS_REENVIO = 120

/**
 * Etapa 1 da recuperação (6.2.2): código de 4 dígitos enviado ao e-mail corporativo. O botão de reenvio
 * fica bloqueado por 2 minutos; o bloqueio também é aplicado pelo servidor, que é quem faz valer a regra.
 */
class CodigoRecuperacaoViewModel(
    private val auth: AuthRepository,
    handle: SavedStateHandle
) : ViewModel() {

    private val login: String = checkNotNull(handle["login"])

    private val _state = MutableStateFlow(CodigoRecuperacaoUiState())
    val state: StateFlow<CodigoRecuperacaoUiState> = _state.asStateFlow()

    init { iniciarContagem() }

    private fun iniciarContagem(segundos: Int = SEGUNDOS_REENVIO) {
        _state.update { it.copy(segundosParaReenviar = segundos) }
        viewModelScope.launch {
            while (_state.value.segundosParaReenviar > 0) {
                kotlinx.coroutines.delay(1_000)
                _state.update { it.copy(segundosParaReenviar = (it.segundosParaReenviar - 1).coerceAtLeast(0)) }
            }
        }
    }

    fun onCodigo(v: String) = _state.update {
        it.copy(codigo = v.filter(Char::isDigit).take(TAMANHO_CODIGO), erro = null)
    }

    fun reenviar() {
        val s = _state.value
        if (s.segundosParaReenviar > 0 || s.reenviando || s.contaBloqueada) return
        _state.update { it.copy(reenviando = true, erro = null, mensagem = null) }
        viewModelScope.launch {
            when (val r = auth.esqueciSenha(login)) {
                is AppResult.Success -> {
                    _state.update { it.copy(reenviando = false, codigo = "", mensagem = "Enviamos um novo código. O anterior deixou de valer.") }
                    iniciarContagem()
                }
                is AppResult.Failure -> _state.update {
                    it.copy(
                        reenviando = false,
                        contaBloqueada = r.error.codigo in CODIGOS_CONTA_BLOQUEADA,
                        erro = r.error.mensagem
                    )
                }
            }
        }
    }

    companion object {
        fun formatarTempo(segundos: Int): String = "%d:%02d".format(segundos / 60, segundos % 60)
    }
}

enum class EtapaSenha { SENHA_ATUAL, CODIGO }

data class AlterarSenhaUiState(
    val etapa: EtapaSenha = EtapaSenha.SENHA_ATUAL,
    val senhaAtual: String = "",
    val codigo: String = "",
    val novaSenha: String = "",
    val confirmacao: String = "",
    val senhaAtualError: String? = null,
    val codigoError: String? = null,
    val novaSenhaError: String? = null,
    val confirmacaoError: String? = null,
    val error: String? = null,
    val mensagem: String? = null,
    val segundosParaReenviar: Int = 0,
    val enviandoCodigo: Boolean = false,
    val loading: Boolean = false,
    val contaBloqueada: Boolean = false,
    val concluido: Boolean = false
)

class AlterarSenhaViewModel(private val auth: AuthRepository) : ViewModel() {

    private val _state = MutableStateFlow(AlterarSenhaUiState())
    val state: StateFlow<AlterarSenhaUiState> = _state.asStateFlow()

    private var contagem: Job? = null

    fun onSenhaAtual(v: String) = _state.update { it.copy(senhaAtual = v, senhaAtualError = null, error = null) }
    fun onCodigo(v: String) = _state.update { it.copy(codigo = v.filter(Char::isDigit).take(TAMANHO_CODIGO), codigoError = null, error = null) }
    fun onNovaSenha(v: String) = _state.update { it.copy(novaSenha = v, novaSenhaError = null, error = null) }
    fun onConfirmacao(v: String) = _state.update { it.copy(confirmacao = v, confirmacaoError = null, error = null) }

    fun enviarCodigo() {
        val s = _state.value
        if (s.enviandoCodigo || s.contaBloqueada) return
        if (s.senhaAtual.isEmpty()) {
            _state.update { it.copy(senhaAtualError = "Informe sua senha atual.") }
            return
        }
        _state.update { it.copy(enviandoCodigo = true, error = null, mensagem = null) }
        viewModelScope.launch {
            when (val r = auth.solicitarCodigoAlteracao(s.senhaAtual)) {
                is AppResult.Success -> {
                    _state.update {
                        it.copy(
                            enviandoCodigo = false,
                            etapa = EtapaSenha.CODIGO,
                            codigo = "",
                            mensagem = if (s.etapa == EtapaSenha.CODIGO) "Enviamos um novo código. O anterior deixou de valer." else null
                        )
                    }
                    iniciarContagem()
                }
                is AppResult.Failure -> tratarFalha(r.error.codigo, r.error.mensagem) { it.copy(enviandoCodigo = false) }
            }
        }
    }

    fun reenviar() {
        if (_state.value.segundosParaReenviar > 0) return
        enviarCodigo()
    }

    fun voltar() {
        contagem?.cancel()
        _state.update { AlterarSenhaUiState(senhaAtual = it.senhaAtual) }
    }

    fun confirmar() {
        val s = _state.value
        if (s.loading || s.contaBloqueada || s.etapa != EtapaSenha.CODIGO) return
        val codigo = if (s.codigo.length != TAMANHO_CODIGO) "Informe o código de $TAMANHO_CODIGO dígitos enviado ao seu e-mail." else null
        val nova = RedefinirSenhaViewModel.validarPolitica(s.novaSenha)
            ?: if (s.novaSenha == s.senhaAtual) "A nova senha deve ser diferente da atual." else null
        val confirmacao = if (s.confirmacao != s.novaSenha) "As senhas não conferem." else null
        if (codigo != null || nova != null || confirmacao != null) {
            _state.update { it.copy(codigoError = codigo, novaSenhaError = nova, confirmacaoError = confirmacao) }
            return
        }
        _state.update { it.copy(loading = true, error = null, mensagem = null) }
        viewModelScope.launch {
            when (val r = auth.alterarSenha(s.senhaAtual, s.codigo, s.novaSenha, s.confirmacao)) {
                is AppResult.Success -> {
                    contagem?.cancel()
                    _state.update { AlterarSenhaUiState(concluido = true) }
                }
                is AppResult.Failure -> tratarFalha(r.error.codigo, r.error.mensagem) { it.copy(loading = false) }
            }
        }
    }

    private fun tratarFalha(codigo: String, mensagem: String, base: (AlterarSenhaUiState) -> AlterarSenhaUiState) {
        if (codigo in CODIGOS_CONTA_BLOQUEADA || codigo == "SENHA_ATUAL_INCORRETA") contagem?.cancel()
        _state.update {
            val s = base(it)
            when {
                codigo in CODIGOS_CONTA_BLOQUEADA -> s.copy(contaBloqueada = true, error = mensagem, segundosParaReenviar = 0)
                codigo == "SENHA_ATUAL_INCORRETA" -> AlterarSenhaUiState(senhaAtualError = mensagem)
                codigo == "CODIGO_INVALIDO" -> s.copy(codigo = "", codigoError = mensagem)
                else -> s.copy(error = mensagem)
            }
        }
    }

    private fun iniciarContagem() {
        contagem?.cancel()
        _state.update { it.copy(segundosParaReenviar = SEGUNDOS_REENVIO) }
        contagem = viewModelScope.launch {
            while (_state.value.segundosParaReenviar > 0) {
                delay(1_000)
                _state.update { it.copy(segundosParaReenviar = (it.segundosParaReenviar - 1).coerceAtLeast(0)) }
            }
        }
    }
}
