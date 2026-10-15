package com.example.avalon_mobile.ui.screens.conteudo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.avalon_mobile.R
import com.example.avalon_mobile.model.Documento
import com.example.avalon_mobile.ui.components.AvalonCard
import com.example.avalon_mobile.ui.components.AvalonTopBar
import com.example.avalon_mobile.ui.components.EmptyState
import com.example.avalon_mobile.ui.components.IconTile
import com.example.avalon_mobile.ui.components.LoadStateContent
import com.example.avalon_mobile.ui.components.LocalSnackbarHostState
import com.example.avalon_mobile.ui.components.StatusPill
import com.example.avalon_mobile.ui.components.SectionTitle
import com.example.avalon_mobile.ui.theme.Background
import com.example.avalon_mobile.ui.theme.BadgeBlueBg
import com.example.avalon_mobile.ui.theme.BadgeBlueText
import com.example.avalon_mobile.ui.theme.BadgeGreenBg
import com.example.avalon_mobile.ui.theme.BadgeGreenText
import com.example.avalon_mobile.ui.theme.BadgeOrangeBg
import com.example.avalon_mobile.ui.theme.BadgeOrangeText
import com.example.avalon_mobile.ui.theme.Border
import com.example.avalon_mobile.ui.theme.BorderSoft
import com.example.avalon_mobile.ui.theme.Inter
import com.example.avalon_mobile.ui.theme.Navy
import com.example.avalon_mobile.ui.theme.TextPrimary
import com.example.avalon_mobile.ui.theme.TextSecondary
import com.example.avalon_mobile.ui.theme.TextStrong
import com.example.avalon_mobile.util.Datas
import com.example.avalon_mobile.util.DocumentOpener
import com.example.avalon_mobile.viewmodel.DocumentosViewModel
import com.example.avalon_mobile.viewmodel.DownloadEvent
import com.example.avalon_mobile.viewmodel.avalonViewModel

@Composable
private fun documentosViewModel() = avalonViewModel { c, _ -> DocumentosViewModel(c.avalonRepository, c.pdfFactory) }

/** Trata abertura do PDF e erros de download de forma única para as duas telas. */
@Composable
private fun DownloadEffects(viewModel: DocumentosViewModel) {
    val context = LocalContext.current
    val snackbar = LocalSnackbarHostState.current
    LaunchedEffect(viewModel) {
        viewModel.events.collect { e ->
            when (e) {
                is DownloadEvent.Abrir -> if (!DocumentOpener.open(context, e.nome, e.bytes)) {
                    snackbar.showSnackbar("Nenhum aplicativo para abrir PDF foi encontrado.")
                }
                is DownloadEvent.Erro -> snackbar.showSnackbar(e.mensagem)
            }
        }
    }
}

@Composable
private fun BuscaDocumentos(valor: String, onChange: (String) -> Unit, placeholder: String) {
    OutlinedTextField(
        value = valor,
        onValueChange = onChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        leadingIcon = { Icon(painterResource(R.drawable.ic_search), null, tint = TextSecondary, modifier = Modifier.size(18.dp)) },
        placeholder = { Text(placeholder, fontFamily = Inter, fontSize = 14.sp, color = TextSecondary) },
        textStyle = TextStyle(fontFamily = Inter, fontSize = 14.sp, color = TextPrimary),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = Border, focusedBorderColor = Navy,
            unfocusedContainerColor = Color.White, focusedContainerColor = Color.White
        )
    )
}

/** Aba Documentos (Sprint 3 — 6.5.4): lista com busca; cadeado = documento indisponibilizado pelo RH. */
@Composable
fun DocumentosScreen(onBack: (() -> Unit)?, viewModel: DocumentosViewModel = documentosViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    DownloadEffects(viewModel)
    Column(Modifier.fillMaxSize().background(Background)) {
        AvalonTopBar("Documentos", onBack = onBack)
        LoadStateContent(state.documentos, onRetry = viewModel::carregar) {
            LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item { BuscaDocumentos(state.busca, viewModel::buscar, "Buscar documento") }
                item { SectionTitle("Meus documentos", Modifier.padding(top = 8.dp)) }
                if (state.filtrados.isEmpty()) item { EmptyState("Nenhum documento encontrado.") }
                items(state.filtrados, key = { it.id }) { doc ->
                    AvalonCard(onClick = { viewModel.baixar(doc) }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconTile(R.drawable.ic_file, Navy, BadgeBlueBg, size = 42.dp, iconSize = 22.dp)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(doc.nome, fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = TextPrimary)
                                Text(
                                    "${doc.tipoDocumento} • ${if (doc.ativo) "Disponível desde ${Datas.dataCurta(doc.dataDisponibilizacao)}" else "Indisponível"}",
                                    fontFamily = Inter, fontSize = 13.sp, color = TextSecondary
                                )
                            }
                            when {
                                state.baixando == doc.id -> CircularProgressIndicator(Modifier.size(18.dp), color = Navy, strokeWidth = 2.dp)
                                !doc.ativo -> Icon(painterResource(R.drawable.ic_lock), "Indisponível", tint = TextSecondary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Downloads (Figma "Downloads"): perfil completo, documentos assinados e extratos/holerites. */
@Composable
fun DownloadsScreen(onBack: () -> Unit, viewModel: DocumentosViewModel = documentosViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    DownloadEffects(viewModel)
    Column(Modifier.fillMaxSize().background(Background)) {
        AvalonTopBar("Downloads", subtitle = "Documentos e extratos disponíveis", onBack = onBack)
        LoadStateContent(state.documentos, onRetry = viewModel::carregar) {
            val ativos = state.filtrados.filter { it.ativo }
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item { BuscaDocumentos(state.busca, viewModel::buscar, "Buscar documentos...") }
                item { Rotulo("Meu Perfil") }
                item {
                    LinhaDownload(
                        titulo = "Baixar Perfil Completo",
                        detalhe = "Exportar dados de registro e GHE • PDF",
                        icone = R.drawable.ic_file_text,
                        carregando = state.baixando == DocumentosViewModel.PERFIL_ID,
                        onClick = viewModel::exportarPerfil
                    )
                }
                val (assinados, extratos) = ativos.partition { it.assinado }
                item { Rotulo("Documentos Assinados") }
                if (assinados.isEmpty()) item { EmptyState("Nenhum documento assinado.") }
                items(assinados, key = { "a${it.id}" }) { doc ->
                    LinhaDownload(doc.nome, "Assinado em ${Datas.dataCurta(doc.dataDisponibilizacao)} • PDF", R.drawable.ic_download,
                        carregando = state.baixando == doc.id, onClick = { viewModel.baixar(doc) }, selo = "Assinado" to (BadgeGreenBg to BadgeGreenText))
                }
                item { Rotulo("Extratos") }
                if (extratos.isEmpty()) item { EmptyState("Nenhum extrato disponível.") }
                items(extratos, key = { "e${it.id}" }) { doc ->
                    val lido = doc.lido == true
                    LinhaDownload(doc.nome, "Disponível • PDF${doc.tamanhoBytes?.let { " (${tamanho(it)})" } ?: ""}", R.drawable.ic_download,
                        carregando = state.baixando == doc.id, onClick = { viewModel.baixar(doc) },
                        selo = if (doc.lido == null) null else (if (lido) "Lido" else "Não Lido") to (if (lido) BadgeBlueBg to BadgeBlueText else BadgeOrangeBg to BadgeOrangeText))
                }
            }
        }
    }
}

@Composable
private fun Rotulo(texto: String) {
    Text(texto, Modifier.padding(top = 8.dp), fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextSecondary)
}

@Composable
private fun LinhaDownload(
    titulo: String,
    detalhe: String,
    icone: Int,
    carregando: Boolean,
    onClick: () -> Unit,
    selo: Pair<String, Pair<Color, Color>>? = null
) {
    AvalonCard(onClick = onClick, border = BorderSoft, contentPadding = 12.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconTile(icone, Navy, BadgeBlueBg, size = 36.dp, iconSize = 18.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(titulo, fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextStrong)
                Spacer(Modifier.height(2.dp))
                Text(detalhe, fontFamily = Inter, fontSize = 11.sp, color = TextSecondary)
            }
            when {
                carregando -> CircularProgressIndicator(Modifier.size(18.dp), color = Navy, strokeWidth = 2.dp)
                selo != null -> StatusPill(selo.first, selo.second.first, selo.second.second, fontSize = 10)
                else -> Icon(painterResource(R.drawable.ic_download), "Baixar", tint = Navy, modifier = Modifier.size(20.dp))
            }
        }
    }
}

private fun tamanho(bytes: Long): String =
    if (bytes >= 1_000_000) "%.1f MB".format(Datas.ptBR, bytes / 1_000_000.0) else "${bytes / 1000} KB"
