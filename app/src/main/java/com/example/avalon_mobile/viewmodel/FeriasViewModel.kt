package com.example.avalon_mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.avalon_mobile.data.AppResult
import com.example.avalon_mobile.data.repository.AvalonRepository
import com.example.avalon_mobile.model.Ferias
import com.example.avalon_mobile.model.FeriasRequest
import com.example.avalon_mobile.ui.components.LoadState
import com.example.avalon_mobile.util.Datas
import com.example.avalon_mobile.util.SaldoFerias
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class FeriasUi(val saldo: SaldoFerias?, val pedidos: List<Ferias>)

sealed interface SolicitacaoState {
    data object Idle : SolicitacaoState
    data object Enviando : SolicitacaoState
    data object Enviada : SolicitacaoState
    data class Erro(val mensagem: String) : SolicitacaoState
}

/** Férias — consulta e solicitação (Sprint 3 — 6.4). */
class FeriasViewModel(private val repository: AvalonRepository) : ViewModel() {

    private val _state = MutableStateFlow<LoadState<FeriasUi>>(LoadState.Loading)
    val state: StateFlow<LoadState<FeriasUi>> = _state.asStateFlow()

    private val _solicitacao = MutableStateFlow<SolicitacaoState>(SolicitacaoState.Idle)
    val solicitacao: StateFlow<SolicitacaoState> = _solicitacao.asStateFlow()

    init { carregar() }

    fun carregar() {
        _state.value = LoadState.Loading
        viewModelScope.launch {
            val perfil = repository.perfil()
            val pedidos = repository.ferias()
            _state.value = when {
                pedidos is AppResult.Failure -> LoadState.Error(pedidos.error.mensagem)
                else -> {
                    val lista = (pedidos as AppResult.Success).data.sortedByDescending { it.dataInicio }
                    val admissao = (perfil as? AppResult.Success)?.data?.dataAdmissao?.let(Datas::parse)
                    LoadState.Content(FeriasUi(admissao?.let { SaldoFerias.calcular(it, lista) }, lista))
                }
            }
        }
    }

    /**
     * Valida no cliente o que o contrato também valida (data_fim >= data_inicio, início futuro,
     * saldo) e envia. Sobreposição de datas é verificada pelo servidor (409 FERIAS_SOBREPOSTAS).
     */
    fun solicitar(inicio: LocalDate, fim: LocalDate, observacao: String) {
        val dias = ChronoUnit.DAYS.between(inicio, fim).toInt() + 1
        val saldo = (state.value as? LoadState.Content)?.data?.saldo?.diasDisponiveis
        val erro = when {
            !inicio.isAfter(LocalDate.now()) -> "A data de início deve ser futura."
            fim.isBefore(inicio) -> "A data final deve ser igual ou posterior à inicial."
            dias < MIN_DIAS -> "O período mínimo é de $MIN_DIAS dias."
            saldo != null && dias > saldo -> "Saldo insuficiente: você tem $saldo dias disponíveis."
            else -> null
        }
        if (erro != null) {
            _solicitacao.value = SolicitacaoState.Erro(erro)
            return
        }
        _solicitacao.value = SolicitacaoState.Enviando
        viewModelScope.launch {
            val request = FeriasRequest(inicio.toString(), fim.toString(), dias, observacao.trim().ifEmpty { null })
            _solicitacao.value = when (val r = repository.solicitarFerias(request)) {
                is AppResult.Success -> SolicitacaoState.Enviada.also { carregar() }
                is AppResult.Failure -> SolicitacaoState.Erro(r.error.mensagem)
            }
        }
    }

    fun resetSolicitacao() { _solicitacao.value = SolicitacaoState.Idle }

    private companion object {
        // CLT art. 134 §1º: nenhum período inferior a 5 dias corridos.
        const val MIN_DIAS = 5
    }
}
