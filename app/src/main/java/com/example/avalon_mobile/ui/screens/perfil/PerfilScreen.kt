package com.example.avalon_mobile.ui.screens.perfil

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import com.example.avalon_mobile.model.CorrecaoRegistrada
import com.example.avalon_mobile.ui.theme.StatusPendingBg
import com.example.avalon_mobile.ui.theme.StatusPendingText
import com.example.avalon_mobile.util.CorrecaoRegras
import com.example.avalon_mobile.util.Datas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.avalon_mobile.R
import com.example.avalon_mobile.model.Colaborador
import com.example.avalon_mobile.ui.components.AvalonCard
import com.example.avalon_mobile.ui.components.AvalonTextField
import com.example.avalon_mobile.ui.components.LoadStateContent
import com.example.avalon_mobile.ui.theme.Background
import com.example.avalon_mobile.ui.theme.Border
import com.example.avalon_mobile.ui.theme.Surface
import com.example.avalon_mobile.ui.theme.NavyFundo
import com.example.avalon_mobile.ui.theme.DayWork
import com.example.avalon_mobile.ui.theme.BorderSoft
import com.example.avalon_mobile.ui.theme.Danger
import com.example.avalon_mobile.ui.theme.DangerContainer
import com.example.avalon_mobile.ui.theme.Inter
import com.example.avalon_mobile.ui.theme.Link
import com.example.avalon_mobile.ui.theme.Navy
import com.example.avalon_mobile.ui.theme.TextPrimary
import com.example.avalon_mobile.ui.theme.TextSecondary
import com.example.avalon_mobile.ui.theme.TextStrong
import com.example.avalon_mobile.viewmodel.CorrecaoState
import com.example.avalon_mobile.viewmodel.PerfilViewModel
import com.example.avalon_mobile.viewmodel.avalonViewModel

/** Perfil (Sprint 2 — 6.5.1) com solicitação de correção cadastral (Sprint 3 — 6.5.2). */
@Composable
fun PerfilScreen(
    onBack: () -> Unit,
    onPrivacidade: () -> Unit,
    onNotificacoes: () -> Unit,
    onTermos: () -> Unit,
    onAlterarSenha: () -> Unit,
    onAparencia: () -> Unit,
    onSair: () -> Unit,
    viewModel: PerfilViewModel = avalonViewModel { c, _ -> PerfilViewModel(c.avalonRepository, c.authRepository, c.correcaoLocal) }
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val correcao by viewModel.correcao.collectAsStateWithLifecycle()
    val historico by viewModel.historico.collectAsStateWithLifecycle()
    var mostrarCorrecao by rememberSaveable { mutableStateOf(false) }
    var confirmarSaida by rememberSaveable { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(Background)) {
        LoadStateContent(state, onRetry = viewModel::carregar) { colaborador ->
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                Cabecalho(colaborador, onBack)
                Column(Modifier.padding(horizontal = 20.dp, vertical = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    InformacoesRegistro(colaborador)
                    TextButton(onClick = { mostrarCorrecao = true }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                        Text("Solicitar correção", fontFamily = Inter, fontSize = 15.sp, color = Navy)
                    }
                    if (historico.isNotEmpty()) MinhasSolicitacoes(historico)
                    AvalonCard(radius = 16.dp, border = BorderSoft, contentPadding = 15.dp) {
                        ItemAjuste(R.drawable.ic_shield_check, "Alterar Senha", onAlterarSenha)
                        ItemAjuste(R.drawable.ic_lock, "Configurações de Privacidade e Anonimato (LGPD)", onPrivacidade)
                        ItemAjuste(R.drawable.ic_bell, "Gerenciar Notificações", onNotificacoes)
                        ItemAjuste(R.drawable.ic_eye, "Aparência e acessibilidade", onAparencia)
                        ItemAjuste(R.drawable.ic_clipboard, "Termos de Uso e Políticas", onTermos)
                        ItemAjuste(R.drawable.ic_log_out, "Sair da Conta", { confirmarSaida = true }, perigo = true)
                    }
                }
            }
        }
    }

    if (mostrarCorrecao) {
        CorrecaoDialog(
            state = correcao,
            onEnviar = viewModel::solicitarCorrecao,
            onFechar = { mostrarCorrecao = false; viewModel.resetCorrecao() }
        )
    }
    if (confirmarSaida) {
        AlertDialog(
            onDismissRequest = { confirmarSaida = false },
            title = { Text("Sair da conta?", fontFamily = Inter, fontWeight = FontWeight.Bold) },
            text = { Text("Sua sessão será encerrada neste aparelho.", fontFamily = Inter) },
            confirmButton = {
                TextButton(onClick = { confirmarSaida = false; viewModel.sair(onSair) }) { Text("Sair", color = Danger, fontFamily = Inter) }
            },
            dismissButton = { TextButton(onClick = { confirmarSaida = false }) { Text("Cancelar", color = Navy, fontFamily = Inter) } },
            containerColor = Surface
        )
    }
}

@Composable
private fun Cabecalho(colaborador: Colaborador, onBack: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(NavyFundo)
            .statusBarsPadding()
            .padding(start = 12.dp, end = 20.dp, top = 16.dp, bottom = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Mesma setinha das demais telas (AvalonTopBar): volta para a Home.
        IconButton(onClick = onBack) {
            Icon(painterResource(R.drawable.ic_arrow_left), contentDescription = "Voltar", tint = Color.White, modifier = Modifier.size(28.dp))
        }
        Spacer(Modifier.width(4.dp))
        Column(Modifier.weight(1f)) {
            Text("Meu Perfil", Modifier.semantics { heading() }, fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, color = Color.White)
            Text("Ajustes e dados funcionais", fontFamily = Inter, fontSize = 13.sp, color = Color.White.copy(alpha = 0.6f))
        }
        // O contrato não expõe foto: avatar com iniciais.
        Box(
            Modifier
                .size(110.dp)
                .semantics { contentDescription = "Iniciais de ${colaborador.nome}" }
                .background(Surface, CircleShape)
                .border(BorderStroke(3.dp, Border), CircleShape)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(Modifier.size(100.dp).background(DayWork, CircleShape), contentAlignment = Alignment.Center) {
                Text(colaborador.iniciais, fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 34.sp, color = Navy)
            }
        }
    }
}

@Composable
private fun InformacoesRegistro(c: Colaborador) {
    AvalonCard(radius = 16.dp, border = BorderSoft, contentPadding = 15.dp) {
        Text("Informações de Registro", fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextStrong)
        Spacer(Modifier.height(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Campo("Nome", c.nome, bloqueado = true)
            Campo("CPF", c.cpfMascarado, bloqueado = true)
            // E-mail é o único campo editável pelo titular, via correção (sem cadeado no protótipo).
            Campo("E-mail", c.email, bloqueado = false)
            Campo("Cargo", c.cargoNome ?: "—", bloqueado = true)
            Campo("Setor", c.setorNome ?: "—", bloqueado = true)
            Campo("Matrícula", c.matricula, bloqueado = true)
        }
    }
}

@Composable
private fun Campo(rotulo: String, valor: String, bloqueado: Boolean) {
    Row(
        Modifier.semantics(mergeDescendants = true) {
            contentDescription = "$rotulo: $valor" + if (bloqueado) ". Somente leitura." else ""
        },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(rotulo.uppercase(), fontFamily = Inter, fontSize = 11.sp, color = TextSecondary)
            Text(valor, fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextStrong)
        }
        if (bloqueado) {
            Icon(painterResource(R.drawable.ic_lock), contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun ItemAjuste(icon: Int, texto: String, onClick: () -> Unit, perigo: Boolean = false) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(if (perigo) DangerContainer else Color.Transparent, RoundedCornerShape(8.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(painterResource(icon), null, tint = if (perigo) Danger else Link, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(12.dp))
        Text(texto, Modifier.weight(1f), fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = if (perigo) Danger else TextStrong)
        Icon(painterResource(R.drawable.ic_chevron_right), null, tint = TextSecondary, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun CorrecaoDialog(state: CorrecaoState, onEnviar: (String, String, String) -> Unit, onFechar: () -> Unit) {
    var campo by rememberSaveable { mutableStateOf(CorrecaoRegras.CAMPOS.first()) }
    var expandido by rememberSaveable { mutableStateOf(false) }
    var valor by rememberSaveable { mutableStateOf("") }
    var justificativa by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onFechar,
        containerColor = Surface,
        title = { Text("Solicitar correção", fontFamily = Inter, fontWeight = FontWeight.Bold, color = TextPrimary) },
        text = {
            if (state is CorrecaoState.Enviada) {
                Text("Solicitação enviada. O RH vai analisar e você será notificado.", fontFamily = Inter, color = TextPrimary)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Seletor com componentes estáveis (o ExposedDropdownMenuBox é experimental).
                    Box {
                        OutlinedButton(
                            onClick = { expandido = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Campo: $campo", Modifier.weight(1f), fontFamily = Inter, color = TextPrimary)
                            Icon(painterResource(R.drawable.ic_chevron_right), null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                        }
                        DropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
                            CorrecaoRegras.CAMPOS.forEach { opcao ->
                                DropdownMenuItem(text = { Text(opcao) }, onClick = { campo = opcao; expandido = false })
                            }
                        }
                    }
                    AvalonTextField(
                        "Valor correto", valor, { valor = it },
                        keyboardType = if (campo == "E-mail") KeyboardType.Email else KeyboardType.Text
                    )
                    AvalonTextField("Justificativa", justificativa, { justificativa = it.take(CorrecaoRegras.JUSTIFICATIVA_MAX) }, singleLine = false, minHeight = 90)
                    Text(
                        "${justificativa.trim().length}/${CorrecaoRegras.JUSTIFICATIVA_MAX} (mínimo ${CorrecaoRegras.JUSTIFICATIVA_MIN})",
                        fontFamily = Inter, fontSize = 12.sp, color = TextSecondary
                    )
                    if (state is CorrecaoState.Erro) Text(
                        state.mensagem, Modifier.semantics { liveRegion = LiveRegionMode.Assertive },
                        color = Danger, fontFamily = Inter, fontSize = 13.sp
                    )
                }
            }
        },
        confirmButton = {
            if (state is CorrecaoState.Enviada) {
                TextButton(onClick = onFechar) { Text("Fechar", color = Navy, fontFamily = Inter) }
            } else {
                TextButton(onClick = { onEnviar(campo, valor, justificativa) }, enabled = state !is CorrecaoState.Enviando) {
                    Text(if (state is CorrecaoState.Enviando) "Enviando…" else "Enviar", color = Navy, fontFamily = Inter)
                }
            }
        },
        dismissButton = {
            if (state !is CorrecaoState.Enviada) TextButton(onClick = onFechar) { Text("Cancelar", color = TextSecondary, fontFamily = Inter) }
        }
    )
}

@Composable
private fun MinhasSolicitacoes(historico: List<CorrecaoRegistrada>) {
    AvalonCard(radius = 16.dp, border = BorderSoft, contentPadding = 15.dp) {
        Text(
            "Minhas solicitações", Modifier.semantics { heading() },
            fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextStrong
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Registro neste aparelho. A resposta do RH chega por notificação.",
            fontFamily = Inter, fontSize = 12.sp, color = TextSecondary
        )
        Spacer(Modifier.height(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            historico.take(5).forEach { item ->
                val status = if (item.status == CorrecaoRegistrada.EM_ANALISE) "Em análise" else item.status
                val data = Datas.dataCurta(item.criadaEm)
                Row(
                    Modifier.semantics(mergeDescendants = true) {
                        contentDescription = "${item.campo}, novo valor ${item.valorSugerido}, $status, enviada em $data"
                    },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(item.campo, fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextStrong)
                        Text(item.valorSugerido, fontFamily = Inter, fontSize = 13.sp, color = TextPrimary)
                        Text(data, fontFamily = Inter, fontSize = 11.sp, color = TextSecondary)
                    }
                    Text(
                        status,
                        Modifier.background(StatusPendingBg, RoundedCornerShape(8.dp)).padding(horizontal = 10.dp, vertical = 4.dp),
                        fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = StatusPendingText
                    )
                }
            }
        }
    }
}
