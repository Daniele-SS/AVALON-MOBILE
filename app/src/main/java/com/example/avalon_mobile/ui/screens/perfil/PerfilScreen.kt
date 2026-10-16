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
    onPrivacidade: () -> Unit,
    onNotificacoes: () -> Unit,
    onTermos: () -> Unit,
    onAlterarSenha: () -> Unit,
    onSair: () -> Unit,
    viewModel: PerfilViewModel = avalonViewModel { c, _ -> PerfilViewModel(c.avalonRepository, c.authRepository) }
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val correcao by viewModel.correcao.collectAsStateWithLifecycle()
    var mostrarCorrecao by rememberSaveable { mutableStateOf(false) }
    var confirmarSaida by rememberSaveable { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(Background)) {
        LoadStateContent(state, onRetry = viewModel::carregar) { colaborador ->
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                Cabecalho(colaborador)
                Column(Modifier.padding(horizontal = 20.dp, vertical = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    InformacoesRegistro(colaborador)
                    TextButton(onClick = { mostrarCorrecao = true }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                        Text("Solicitar correção", fontFamily = Inter, fontSize = 15.sp, color = Navy)
                    }
                    AvalonCard(radius = 16.dp, border = BorderSoft, contentPadding = 15.dp) {
                        ItemAjuste(R.drawable.ic_shield_check, "Alterar Senha", onAlterarSenha)
                        ItemAjuste(R.drawable.ic_lock, "Configurações de Privacidade e Anonimato (LGPD)", onPrivacidade)
                        ItemAjuste(R.drawable.ic_bell, "Gerenciar Notificações", onNotificacoes)
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
            containerColor = Color.White
        )
    }
}

@Composable
private fun Cabecalho(colaborador: Colaborador) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(Navy)
            .statusBarsPadding()
            .padding(start = 26.dp, end = 20.dp, top = 16.dp, bottom = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text("Meu Perfil", fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, color = Color.White)
            Text("Ajustes e dados funcionais", fontFamily = Inter, fontSize = 13.sp, color = Color.White.copy(alpha = 0.6f))
        }
        // O contrato não expõe foto: avatar com iniciais.
        Box(
            Modifier
                .size(110.dp)
                .background(Color.White, CircleShape)
                .border(BorderStroke(3.dp, Color(0xFFCECECE)), CircleShape)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(Modifier.size(100.dp).background(Color(0xFFBADAFF), CircleShape), contentAlignment = Alignment.Center) {
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
            Campo("GHE", c.ghe ?: "—", bloqueado = true)
            Campo("Matrícula", c.matricula, bloqueado = true)
        }
    }
}

@Composable
private fun Campo(rotulo: String, valor: String, bloqueado: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(rotulo.uppercase(), fontFamily = Inter, fontSize = 11.sp, color = TextSecondary)
            Text(valor, fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextStrong)
        }
        if (bloqueado) {
            Icon(painterResource(R.drawable.ic_lock), contentDescription = "Somente leitura", tint = TextSecondary, modifier = Modifier.size(16.dp))
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
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(painterResource(icon), null, tint = if (perigo) Danger else Link, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(12.dp))
        Text(texto, Modifier.weight(1f), fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = if (perigo) Danger else TextStrong)
        Icon(painterResource(R.drawable.ic_chevron_right), null, tint = TextSecondary, modifier = Modifier.size(16.dp))
    }
}

private val camposCorrigiveis = listOf("Nome", "CPF", "E-mail", "Cargo", "Setor", "GHE", "Matrícula")

@Composable
private fun CorrecaoDialog(state: CorrecaoState, onEnviar: (String, String, String) -> Unit, onFechar: () -> Unit) {
    var campo by rememberSaveable { mutableStateOf(camposCorrigiveis.first()) }
    var expandido by rememberSaveable { mutableStateOf(false) }
    var valor by rememberSaveable { mutableStateOf("") }
    var justificativa by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onFechar,
        containerColor = Color.White,
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
                            camposCorrigiveis.forEach { opcao ->
                                DropdownMenuItem(text = { Text(opcao) }, onClick = { campo = opcao; expandido = false })
                            }
                        }
                    }
                    AvalonTextField("Valor correto", valor, { valor = it })
                    AvalonTextField("Justificativa", justificativa, { justificativa = it }, singleLine = false, minHeight = 90)
                    if (state is CorrecaoState.Erro) Text(state.mensagem, color = Danger, fontFamily = Inter, fontSize = 13.sp)
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
