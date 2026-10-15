package com.example.avalon_mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.avalon_mobile.data.AppResult
import com.example.avalon_mobile.data.repository.AuthRepository
import com.example.avalon_mobile.data.repository.AvalonRepository
import com.example.avalon_mobile.model.Comunicado
import com.example.avalon_mobile.model.Notificacao
import com.example.avalon_mobile.ui.components.LoadState
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUi(
    val primeiroNome: String,
    val pesquisasPendentes: Int,
    val feriasPendentes: Int,
    val feedbacksNovos: Int,
    val notificacoesNaoLidas: Int,
    val comunicados: List<Comunicado>
)

/** Dashboard principal e atalhos (Sprint 4 — 6.3.1 e 6.3.3) + mural de avisos (6.3.2). */
class HomeViewModel(
    private val repository: AvalonRepository,
    private val auth: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow<LoadState<HomeUi>>(LoadState.Loading)
    val state: StateFlow<LoadState<HomeUi>> = _state.asStateFlow()

    fun carregar() {
        if (_state.value !is LoadState.Content) _state.value = LoadState.Loading
        viewModelScope.launch {
            val dashboard = async { repository.dashboard() }
            val feedbacks = async { repository.feedbacks() }
            val notificacoes = async { repository.notificacoes() }
            val comunicados = async { repository.comunicados() }
            _state.value = when (val d = dashboard.await()) {
                is AppResult.Failure -> LoadState.Error(d.error.mensagem)
                is AppResult.Success -> {
                    val c = d.data.colaborador
                    LoadState.Content(
                        HomeUi(
                            primeiroNome = auth.usuario.value?.nome?.substringBefore(" ") ?: "colaborador",
                            pesquisasPendentes = (c?.pesquisasPendentes ?: 0) + (c?.avaliacoesPendentes ?: 0),
                            feriasPendentes = c?.feriasEmAndamento ?: 0,
                            feedbacksNovos = (feedbacks.await() as? AppResult.Success)?.data?.count { it.lido == false } ?: 0,
                            notificacoesNaoLidas = (notificacoes.await() as? AppResult.Success)?.data?.count { it.lida == false } ?: 0,
                            comunicados = (comunicados.await() as? AppResult.Success)?.data.orEmpty()
                        )
                    )
                }
            }
        }
    }
}

/** Central de notificações (Sprint 4 — 6.3.4). Lista só as do titular do token (RF-534). */
class NotificacoesViewModel(private val repository: AvalonRepository) : ViewModel() {
    private val _state = MutableStateFlow<LoadState<List<Pair<String, List<Notificacao>>>>>(LoadState.Loading)
    val state: StateFlow<LoadState<List<Pair<String, List<Notificacao>>>>> = _state.asStateFlow()

    init { carregar() }

    fun carregar() {
        _state.value = LoadState.Loading
        viewModelScope.launch {
            _state.value = when (val r = repository.notificacoes()) {
                // Mais recentes primeiro (RF-504); sem data no DDL, o servidor ordena por id decrescente.
                is AppResult.Success -> LoadState.Content(
                    r.data.sortedByDescending { it.id }
                        .groupBy { it.dataHora?.substringBefore(",")?.trim() ?: "Recentes" }
                        .toList()
                )
                is AppResult.Failure -> LoadState.Error(r.error.mensagem)
            }
        }
    }
}

class ComunicadosViewModel(private val repository: AvalonRepository) : ViewModel() {
    private val _state = MutableStateFlow<LoadState<List<Comunicado>>>(LoadState.Loading)
    val state: StateFlow<LoadState<List<Comunicado>>> = _state.asStateFlow()

    init { carregar() }

    fun carregar() {
        _state.value = LoadState.Loading
        viewModelScope.launch { _state.value = repository.comunicados().toLoadState() }
    }
}
