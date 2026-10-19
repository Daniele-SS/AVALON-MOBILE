package com.example.avalon_mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.avalon_mobile.data.local.ApiConfig
import com.example.avalon_mobile.data.local.SessionManager
import com.example.avalon_mobile.data.remote.ResultadoTeste
import com.example.avalon_mobile.data.remote.ServidorTester
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.HttpUrl

data class ServidorUiState(
    val endereco: String,
    val atual: String,
    val padrao: String,
    val enderecoError: String? = null,
    val mensagem: String? = null,
    val sucesso: Boolean = false,
    val testando: Boolean = false
)

class ServidorViewModel(
    private val config: ApiConfig,
    private val tester: ServidorTester,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(
        ServidorUiState(endereco = config.atual.toString(), atual = config.atual.toString(), padrao = config.padrao.toString())
    )
    val state: StateFlow<ServidorUiState> = _state.asStateFlow()

    fun onEndereco(v: String) = _state.update { it.copy(endereco = v, enderecoError = null, mensagem = null) }

    fun testar() {
        val url = validar() ?: return
        _state.update { it.copy(testando = true, mensagem = null) }
        viewModelScope.launch {
            val resultado = tester.testar(url)
            _state.update {
                it.copy(
                    testando = false,
                    sucesso = resultado is ResultadoTeste.Avalon,
                    mensagem = when (resultado) {
                        ResultadoTeste.Avalon -> "A API do AVALON respondeu em $url."
                        is ResultadoTeste.OutroServico -> "O endereço respondeu (HTTP ${resultado.status}), mas não parece ser a API do AVALON."
                        ResultadoTeste.SemResposta -> "Sem resposta de $url. Confira o IP, a porta e se a API está rodando."
                    }
                )
            }
        }
    }

    fun salvar() {
        val url = validar() ?: return
        aplicar(url, "Endereço salvo: $url") { config.salvar(url) }
    }

    fun restaurarPadrao() = aplicar(config.padrao, "Endereço padrão restaurado: ${config.padrao}") { config.restaurarPadrao() }

    private fun aplicar(url: HttpUrl, mensagem: String, gravar: () -> Unit) {
        if (url != config.atual) sessionManager.end()
        gravar()
        _state.update {
            it.copy(endereco = url.toString(), atual = url.toString(), enderecoError = null, sucesso = true, mensagem = mensagem)
        }
    }

    private fun validar(): HttpUrl? {
        val url = ApiConfig.normalizar(_state.value.endereco)
        if (url == null) {
            _state.update { it.copy(enderecoError = "Endereço inválido. Exemplo: http://192.168.0.10:8080/", mensagem = null) }
        }
        return url
    }
}
