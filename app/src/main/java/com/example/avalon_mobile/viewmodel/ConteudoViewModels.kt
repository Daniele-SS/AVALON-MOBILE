package com.example.avalon_mobile.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.avalon_mobile.data.AppResult
import com.example.avalon_mobile.data.repository.AvalonRepository
import com.example.avalon_mobile.model.BeneficioItem
import com.example.avalon_mobile.model.Documento
import com.example.avalon_mobile.model.Feedback
import com.example.avalon_mobile.model.RespostaFeedback
import com.example.avalon_mobile.ui.components.LoadState
import com.example.avalon_mobile.util.PdfFactory
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class FiltroBeneficio(val rotulo: String) { TODOS("Todos"), ADERIDOS("Aderidos"), DISPONIVEIS("Disponíveis") }

class BeneficiosViewModel(private val repository: AvalonRepository) : ViewModel() {
    private val _state = MutableStateFlow<LoadState<List<BeneficioItem>>>(LoadState.Loading)
    val state: StateFlow<LoadState<List<BeneficioItem>>> = _state.asStateFlow()

    private val _filtro = MutableStateFlow(FiltroBeneficio.TODOS)
    val filtro: StateFlow<FiltroBeneficio> = _filtro.asStateFlow()

    private val _mensagens = Channel<String>(Channel.BUFFERED)
    val mensagens = _mensagens.receiveAsFlow()

    init { carregar() }

    fun carregar() {
        _state.value = LoadState.Loading
        viewModelScope.launch { _state.value = repository.beneficios().toLoadState() }
    }

    fun filtrar(f: FiltroBeneficio) { _filtro.value = f }

    /** Adesão/cancelamento são decididos pelo RH (concessões); o app só registra o pedido. */
    fun solicitar(item: BeneficioItem) {
        viewModelScope.launch {
            val acao = if (item.aderido) "cancelamento" else "adesão"
            _mensagens.send(
                when (val r = repository.solicitarBeneficio(item)) {
                    is AppResult.Success -> "Pedido de $acao de ${item.beneficio.nome} encaminhado ao RH."
                    is AppResult.Failure -> r.error.mensagem
                }
            )
        }
    }
}

sealed interface DownloadEvent {
    data class Abrir(val nome: String, val bytes: ByteArray) : DownloadEvent
    data class Erro(val mensagem: String) : DownloadEvent
}

data class DocumentosUi(
    val busca: String = "",
    val documentos: LoadState<List<Documento>> = LoadState.Loading,
    val baixando: Int? = null
) {
    val filtrados: List<Documento>
        get() = (documentos as? LoadState.Content)?.data.orEmpty()
            .filter { busca.isBlank() || it.nome.contains(busca.trim(), ignoreCase = true) || it.tipoDocumento.contains(busca.trim(), ignoreCase = true) }
}

/** Documentos e holerites (Sprint 3 — 6.5.4). Compartilhado pelas telas Documentos e Downloads. */
class DocumentosViewModel(
    private val repository: AvalonRepository,
    private val pdfFactory: PdfFactory
) : ViewModel() {

    private val _state = MutableStateFlow(DocumentosUi())
    val state: StateFlow<DocumentosUi> = _state.asStateFlow()

    private val _events = Channel<DownloadEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init { carregar() }

    fun carregar() {
        _state.update { it.copy(documentos = LoadState.Loading) }
        viewModelScope.launch {
            val r = repository.documentos().toLoadState()
            _state.update { it.copy(documentos = r) }
        }
    }

    fun buscar(texto: String) = _state.update { it.copy(busca = texto) }

    fun baixar(documento: Documento) {
        if (!documento.ativo) {
            viewModelScope.launch { _events.send(DownloadEvent.Erro("Documento indisponível. Procure o RH.")) }
            return
        }
        _state.update { it.copy(baixando = documento.id) }
        viewModelScope.launch {
            val evento = when (val r = repository.baixarDocumento(documento)) {
                is AppResult.Success -> DownloadEvent.Abrir(documento.nome, r.data)
                is AppResult.Failure -> DownloadEvent.Erro(r.error.mensagem)
            }
            _state.update { it.copy(baixando = null) }
            _events.send(evento)
        }
    }

    /** "Baixar Perfil Completo": PDF gerado no aparelho a partir do cadastro (sem rota dedicada no contrato). */
    fun exportarPerfil() {
        _state.update { it.copy(baixando = PERFIL_ID) }
        viewModelScope.launch {
            val evento = when (val r = repository.perfil()) {
                is AppResult.Success -> DownloadEvent.Abrir("perfil_${r.data.matricula}", pdfFactory.perfilCompleto(r.data))
                is AppResult.Failure -> DownloadEvent.Erro(r.error.mensagem)
            }
            _state.update { it.copy(baixando = null) }
            _events.send(evento)
        }
    }

    companion object {
        const val PERFIL_ID = -1
    }
}

class FeedbacksViewModel(private val repository: AvalonRepository) : ViewModel() {
    private val _state = MutableStateFlow<LoadState<List<Feedback>>>(LoadState.Loading)
    val state: StateFlow<LoadState<List<Feedback>>> = _state.asStateFlow()

    init { carregar() }

    fun carregar() {
        _state.value = LoadState.Loading
        viewModelScope.launch { _state.value = repository.feedbacks().toLoadState() }
    }
}

data class FeedbackDetalheUi(
    val feedback: LoadState<Feedback> = LoadState.Loading,
    val respostas: List<RespostaFeedback> = emptyList(),
    val texto: String = "",
    val enviando: Boolean = false,
    val erro: String? = null,
    val enviado: Boolean = false
)

/** Detalhe e resposta de feedback (Sprint 3/4 — 6.7). */
class FeedbackDetalheViewModel(
    private val repository: AvalonRepository,
    handle: SavedStateHandle
) : ViewModel() {

    private val id: Int = checkNotNull(handle.get<Int>("id"))

    private val _state = MutableStateFlow(FeedbackDetalheUi())
    val state: StateFlow<FeedbackDetalheUi> = _state.asStateFlow()

    init { carregar() }

    fun carregar() {
        _state.update { it.copy(feedback = LoadState.Loading) }
        viewModelScope.launch {
            val feedback = when (val r = repository.feedbacks()) {
                is AppResult.Success -> r.data.firstOrNull { it.id == id }
                    ?.let { LoadState.Content(it) } ?: LoadState.Error("Feedback não encontrado.")
                is AppResult.Failure -> LoadState.Error(r.error.mensagem)
            }
            val respostas = (repository.respostasFeedback(id) as? AppResult.Success)?.data.orEmpty()
            _state.update { it.copy(feedback = feedback, respostas = respostas) }
        }
    }

    fun onTexto(v: String) = _state.update { it.copy(texto = v.take(MAX), erro = null, enviado = false) }

    fun enviar() {
        val texto = _state.value.texto.trim()
        if (texto.isEmpty()) {
            _state.update { it.copy(erro = "Escreva sua resposta.") }
            return
        }
        _state.update { it.copy(enviando = true) }
        viewModelScope.launch {
            when (val r = repository.responderFeedback(id, texto)) {
                is AppResult.Success -> _state.update { it.copy(enviando = false, texto = "", enviado = true, respostas = it.respostas + r.data) }
                is AppResult.Failure -> _state.update { it.copy(enviando = false, erro = r.error.mensagem) }
            }
        }
    }

    private companion object { const val MAX = 1000 }
}
