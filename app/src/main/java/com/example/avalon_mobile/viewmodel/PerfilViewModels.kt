package com.example.avalon_mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.avalon_mobile.data.AppResult
import com.example.avalon_mobile.data.local.CorrecaoLocal
import com.example.avalon_mobile.data.repository.AuthRepository
import com.example.avalon_mobile.data.repository.AvalonRepository
import com.example.avalon_mobile.model.Colaborador
import com.example.avalon_mobile.model.CorrecaoRegistrada
import com.example.avalon_mobile.model.DiaSemana
import com.example.avalon_mobile.model.JornadaEscala
import com.example.avalon_mobile.model.SolicitacaoCorrecao
import com.example.avalon_mobile.ui.components.LoadState
import com.example.avalon_mobile.util.CorrecaoRegras
import com.example.avalon_mobile.util.Datas
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters

sealed interface CorrecaoState {
    data object Idle : CorrecaoState
    data object Enviando : CorrecaoState
    data object Enviada : CorrecaoState
    data class Erro(val mensagem: String) : CorrecaoState
}

class PerfilViewModel(
    private val repository: AvalonRepository,
    private val auth: AuthRepository,
    private val historicoLocal: CorrecaoLocal? = null
) : ViewModel() {

    private val _state = MutableStateFlow<LoadState<Colaborador>>(LoadState.Loading)
    val state: StateFlow<LoadState<Colaborador>> = _state.asStateFlow()

    private val _correcao = MutableStateFlow<CorrecaoState>(CorrecaoState.Idle)
    val correcao: StateFlow<CorrecaoState> = _correcao.asStateFlow()

    private val _historico = MutableStateFlow<List<CorrecaoRegistrada>>(emptyList())
    val historico: StateFlow<List<CorrecaoRegistrada>> = _historico.asStateFlow()

    private var idColaborador: Int? = null

    init { carregar() }

    fun carregar() {
        _state.value = LoadState.Loading
        viewModelScope.launch {
            val r = repository.perfil()
            if (r is AppResult.Success) {
                idColaborador = r.data.id
                _historico.value = historicoLocal?.listar(r.data.id).orEmpty()
            }
            _state.value = r.toLoadState()
        }
    }

    /** Solicitação de correção cadastral (Sprint 3 — 6.5.2 / RF-006). */
    fun solicitarCorrecao(campo: String, valor: String, justificativa: String) {
        CorrecaoRegras.validar(campo, valor, justificativa)?.let {
            _correcao.value = CorrecaoState.Erro(it)
            return
        }
        if (CorrecaoRegras.pendentePara(campo, _historico.value)) {
            _correcao.value = CorrecaoState.Erro("Já existe uma solicitação em análise para \"$campo\". Aguarde a resposta do RH.")
            return
        }
        _correcao.value = CorrecaoState.Enviando
        viewModelScope.launch {
            val pedido = SolicitacaoCorrecao(campo, valor.trim(), justificativa.trim())
            _correcao.value = when (val r = repository.solicitarCorrecao(pedido)) {
                is AppResult.Success -> {
                    registrar(pedido)
                    CorrecaoState.Enviada
                }
                is AppResult.Failure -> CorrecaoState.Erro(r.error.mensagem)
            }
        }
    }

    private fun registrar(pedido: SolicitacaoCorrecao) {
        val id = idColaborador ?: return
        val registro = CorrecaoRegistrada(
            id = System.currentTimeMillis(),
            campo = pedido.campo,
            valorSugerido = pedido.valorSugerido,
            justificativa = pedido.justificativa,
            criadaEm = LocalDateTime.now().toString()
        )
        historicoLocal?.registrar(id, registro)
        _historico.value = CorrecaoRegras.adicionar(_historico.value, registro)
    }

    fun resetCorrecao() { _correcao.value = CorrecaoState.Idle }

    fun sair(onDone: () -> Unit) {
        viewModelScope.launch {
            auth.logout()
            onDone()
        }
    }
}

enum class TipoDia { TRABALHO, HOJE, FOLGA }

data class DiaEscala(val data: LocalDate, val sigla: String, val tipo: TipoDia, val trabalha: Boolean)

data class JornadaUi(val jornada: JornadaEscala?, val semana: List<DiaEscala>)

class JornadaViewModel(private val repository: AvalonRepository) : ViewModel() {

    private val _state = MutableStateFlow<LoadState<JornadaUi>>(LoadState.Loading)
    val state: StateFlow<LoadState<JornadaUi>> = _state.asStateFlow()

    init { carregar() }

    fun carregar() {
        _state.value = LoadState.Loading
        viewModelScope.launch {
            _state.value = when (val r = repository.jornada()) {
                is AppResult.Success -> LoadState.Content(
                    r.data?.let { (jornada, dias) -> JornadaUi(jornada, montarSemana(dias)) } ?: JornadaUi(null, emptyList())
                )
                is AppResult.Failure -> LoadState.Error(r.error.mensagem)
            }
        }
    }

    companion object {
        /** Semana corrente (seg–dom) cruzada com os dias ativos da escala (`DiaSemana.ativo`). */
        fun montarSemana(dias: List<DiaSemana>, hoje: LocalDate = LocalDate.now()): List<DiaEscala> {
            val ativos = dias.filter { it.ativo }.map { it.diaSigla.uppercase() }.toSet()
            val segunda = hoje.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            return (0L..6L).map { offset ->
                val data = segunda.plusDays(offset)
                val sigla = Datas.sigla(data.dayOfWeek)
                val trabalha = sigla in ativos
                val tipo = when {
                    data == hoje -> TipoDia.HOJE
                    trabalha -> TipoDia.TRABALHO
                    else -> TipoDia.FOLGA
                }
                DiaEscala(data, sigla, tipo, trabalha)
            }
        }
    }
}
