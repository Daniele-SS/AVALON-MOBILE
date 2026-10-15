package com.example.avalon_mobile.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.avalon_mobile.data.AppResult
import com.example.avalon_mobile.data.repository.AvalonRepository
import com.example.avalon_mobile.model.Avaliacao
import com.example.avalon_mobile.model.AvaliacaoPergunta
import com.example.avalon_mobile.model.Pergunta
import com.example.avalon_mobile.model.Pesquisa
import com.example.avalon_mobile.model.RespostaAvaliacaoItem
import com.example.avalon_mobile.model.RespostaPesquisaItem
import com.example.avalon_mobile.ui.components.LoadState
import com.example.avalon_mobile.util.Datas
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class PesquisasUi(
    val pendentes: List<Pesquisa>,
    val respondidas: List<Pesquisa>,
    val avaliacao: Avaliacao?
) {
    val totalPendentes: Int get() = pendentes.size + (if (avaliacao != null) 1 else 0)
}

/** Lista de pesquisas e avaliações (Sprint 4 — 6.6). */
class PesquisasViewModel(private val repository: AvalonRepository) : ViewModel() {
    private val _state = MutableStateFlow<LoadState<PesquisasUi>>(LoadState.Loading)
    val state: StateFlow<LoadState<PesquisasUi>> = _state.asStateFlow()

    fun carregar() {
        if (_state.value !is LoadState.Content) _state.value = LoadState.Loading
        viewModelScope.launch {
            val pesquisas = repository.pesquisas()
            val avaliacao = (repository.avaliacaoPendente() as? AppResult.Success)?.data
            _state.value = when (pesquisas) {
                is AppResult.Failure -> LoadState.Error(pesquisas.error.mensagem)
                is AppResult.Success -> {
                    val hoje = LocalDate.now()
                    val (pendentes, outras) = pesquisas.data.partition { p ->
                        p.respondida != true && p.status && Datas.parse(p.dataFim)?.isBefore(hoje) != true
                    }
                    LoadState.Content(PesquisasUi(pendentes.sortedBy { it.dataFim }, outras.sortedByDescending { it.dataFim }, avaliacao))
                }
            }
        }
    }
}

enum class EtapaPesquisa { PERGUNTAS, REVISAO, ENVIADA }

data class PesquisaUi(
    val pesquisa: Pesquisa,
    val perguntas: List<Pergunta>,
    val respostas: Map<Int, String> = emptyMap(),
    val indice: Int = 0,
    val etapa: EtapaPesquisa = EtapaPesquisa.PERGUNTAS,
    /** 6.6: alerta de pergunta obrigatória sem resposta. */
    val alertaObrigatoria: Boolean = false,
    val confirmarEnvio: Boolean = false,
    val enviando: Boolean = false,
    val erro: String? = null,
    val voltarParaRevisao: Boolean = false
) {
    val atual: Pergunta? get() = perguntas.getOrNull(indice)
    val obrigatoriasPendentes: List<Pergunta>
        get() = perguntas.filter { it.obrigatorio && respostas[it.id].isNullOrBlank() }
}

/** Resposta de pesquisa: pergunta a pergunta, revisão e envio em lote (contrato: `RespostaPesquisaLoteInput`). */
class PesquisaViewModel(
    private val repository: AvalonRepository,
    handle: SavedStateHandle
) : ViewModel() {

    private val id: Int = checkNotNull(handle.get<Int>("id"))

    private val _state = MutableStateFlow<LoadState<PesquisaUi>>(LoadState.Loading)
    val state: StateFlow<LoadState<PesquisaUi>> = _state.asStateFlow()

    init { carregar() }

    fun carregar() {
        _state.value = LoadState.Loading
        viewModelScope.launch {
            _state.value = when (val r = repository.pesquisa(id)) {
                is AppResult.Success -> LoadState.Content(PesquisaUi(r.data.first, r.data.second))
                is AppResult.Failure -> LoadState.Error(r.error.mensagem)
            }
        }
    }

    private fun edit(block: (PesquisaUi) -> PesquisaUi) = _state.update { s -> if (s is LoadState.Content) LoadState.Content(block(s.data)) else s }

    fun responder(idPergunta: Int, valor: String) = edit { it.copy(respostas = it.respostas + (idPergunta to valor), alertaObrigatoria = false) }

    fun proxima() = edit { ui ->
        val atual = ui.atual ?: return@edit ui
        when {
            atual.obrigatorio && ui.respostas[atual.id].isNullOrBlank() -> ui.copy(alertaObrigatoria = true)
            ui.voltarParaRevisao || ui.indice == ui.perguntas.lastIndex -> ui.copy(etapa = EtapaPesquisa.REVISAO, voltarParaRevisao = false)
            else -> ui.copy(indice = ui.indice + 1)
        }
    }

    /** Retorna false quando já está na primeira pergunta (a tela decide sair). */
    fun anterior(): Boolean {
        val ui = (_state.value as? LoadState.Content)?.data ?: return false
        if (ui.etapa == EtapaPesquisa.ENVIADA) return false
        if (ui.etapa == EtapaPesquisa.REVISAO) { edit { it.copy(etapa = EtapaPesquisa.PERGUNTAS) }; return true }
        if (ui.indice == 0) return false
        edit { it.copy(indice = it.indice - 1, alertaObrigatoria = false) }
        return true
    }

    fun editar(indice: Int) = edit { it.copy(indice = indice, etapa = EtapaPesquisa.PERGUNTAS, voltarParaRevisao = true) }

    fun pedirConfirmacao() = edit { ui ->
        if (ui.obrigatoriasPendentes.isNotEmpty()) ui.copy(erro = "Responda as perguntas obrigatórias marcadas com *.")
        else ui.copy(confirmarEnvio = true, erro = null)
    }

    fun cancelarConfirmacao() = edit { it.copy(confirmarEnvio = false) }

    fun enviar() {
        val ui = (_state.value as? LoadState.Content)?.data ?: return
        edit { it.copy(confirmarEnvio = false, enviando = true, erro = null) }
        viewModelScope.launch {
            val itens = ui.perguntas.mapNotNull { p -> ui.respostas[p.id]?.takeIf { it.isNotBlank() }?.let { RespostaPesquisaItem(p.id, it.trim()) } }
            when (val r = repository.responderPesquisa(id, itens)) {
                is AppResult.Success -> edit { it.copy(enviando = false, etapa = EtapaPesquisa.ENVIADA) }
                is AppResult.Failure -> edit { it.copy(enviando = false, erro = r.error.mensagem) }
            }
        }
    }

    companion object {
        /** Escala Likert de concordância (perguntas `ESCALA`, valores "1"–"5"). */
        val ESCALA = listOf("1" to "Discordo totalmente", "2" to "Discordo", "3" to "Neutro", "4" to "Concordo", "5" to "Concordo totalmente")
    }
}

enum class EtapaAvaliacao { INTRO, PERGUNTAS, ENVIADA }

data class AvaliacaoUi(
    val avaliacao: Avaliacao,
    val perguntas: List<AvaliacaoPergunta> = emptyList(),
    val respostas: Map<Int, Int> = emptyMap(),
    val indice: Int = 0,
    val etapa: EtapaAvaliacao = EtapaAvaliacao.INTRO,
    val alertaObrigatoria: Boolean = false,
    val carregandoPerguntas: Boolean = false,
    val enviando: Boolean = false,
    val erro: String? = null
) {
    val atual: AvaliacaoPergunta? get() = perguntas.getOrNull(indice)
    val progresso: Float get() = if (perguntas.isEmpty()) 0f else (indice + 1f) / perguntas.size
}

/**
 * Avaliação de fatores psicossociais (NR-1). Anônima: o envio é em lote e a confirmação
 * não traz identificadores; nenhuma resposta individual é exibida ou guardada no aparelho.
 */
class AvaliacaoViewModel(private val repository: AvalonRepository) : ViewModel() {

    private val _state = MutableStateFlow<LoadState<AvaliacaoUi?>>(LoadState.Loading)
    val state: StateFlow<LoadState<AvaliacaoUi?>> = _state.asStateFlow()

    init { carregar() }

    fun carregar() {
        _state.value = LoadState.Loading
        viewModelScope.launch {
            _state.value = when (val r = repository.avaliacaoPendente()) {
                is AppResult.Success -> LoadState.Content(r.data?.let { AvaliacaoUi(it) })
                is AppResult.Failure -> LoadState.Error(r.error.mensagem)
            }
        }
    }

    private fun edit(block: (AvaliacaoUi) -> AvaliacaoUi) =
        _state.update { s -> if (s is LoadState.Content && s.data != null) LoadState.Content(block(s.data)) else s }

    private val ui: AvaliacaoUi? get() = (_state.value as? LoadState.Content)?.data

    fun iniciar() {
        val atual = ui ?: return
        edit { it.copy(carregandoPerguntas = true, erro = null) }
        viewModelScope.launch {
            when (val r = repository.perguntasAvaliacao(atual.avaliacao.id)) {
                is AppResult.Success -> edit { it.copy(perguntas = r.data, etapa = EtapaAvaliacao.PERGUNTAS, carregandoPerguntas = false) }
                is AppResult.Failure -> edit { it.copy(carregandoPerguntas = false, erro = r.error.mensagem) }
            }
        }
    }

    fun responder(valor: Int) = edit { u -> u.atual?.let { u.copy(respostas = u.respostas + (it.id to valor), alertaObrigatoria = false) } ?: u }

    fun continuar() {
        val u = ui ?: return
        val atual = u.atual ?: return
        if (atual.obrigatorio && u.respostas[atual.id] == null) { edit { it.copy(alertaObrigatoria = true) }; return }
        if (u.indice < u.perguntas.lastIndex) { edit { it.copy(indice = it.indice + 1) }; return }
        edit { it.copy(enviando = true, erro = null) }
        viewModelScope.launch {
            val itens = u.perguntas.mapNotNull { p -> u.respostas[p.id]?.let { RespostaAvaliacaoItem(p.id, it) } }
            when (val r = repository.responderAvaliacao(u.avaliacao.id, itens)) {
                // Descarta as respostas da memória assim que o servidor confirma.
                is AppResult.Success -> edit { it.copy(enviando = false, respostas = emptyMap(), etapa = EtapaAvaliacao.ENVIADA) }
                is AppResult.Failure -> edit { it.copy(enviando = false, erro = r.error.mensagem) }
            }
        }
    }

    fun voltar(): Boolean {
        val u = ui ?: return false
        if (u.etapa == EtapaAvaliacao.PERGUNTAS && u.indice > 0) { edit { it.copy(indice = it.indice - 1, alertaObrigatoria = false) }; return true }
        return false
    }

    companion object {
        /** Escala de frequência 1–5 do protótipo; a interpretação de risco é feita no servidor (Motor de Regras). */
        val FREQUENCIA = listOf(1 to "Nunca", 2 to "Raramente", 3 to "Às vezes", 4 to "Frequentemente", 5 to "Sempre")
    }
}
