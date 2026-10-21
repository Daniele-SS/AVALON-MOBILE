package com.example.avalon_mobile.ui.screens.perfil

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.avalon_mobile.ui.components.AvalonCard
import com.example.avalon_mobile.ui.components.AvalonPrimaryButton
import com.example.avalon_mobile.ui.components.AvalonTextField
import com.example.avalon_mobile.ui.components.AvalonTopBar
import com.example.avalon_mobile.ui.screens.auth.Banner
import com.example.avalon_mobile.ui.theme.Background
import com.example.avalon_mobile.ui.theme.BorderSoft
import com.example.avalon_mobile.ui.theme.Inter
import com.example.avalon_mobile.ui.theme.Navy
import com.example.avalon_mobile.ui.theme.TextSecondary
import com.example.avalon_mobile.viewmodel.AlterarSenhaUiState
import com.example.avalon_mobile.viewmodel.AlterarSenhaViewModel
import com.example.avalon_mobile.viewmodel.CodigoRecuperacaoViewModel
import com.example.avalon_mobile.viewmodel.EtapaSenha
import com.example.avalon_mobile.viewmodel.TAMANHO_CODIGO
import com.example.avalon_mobile.viewmodel.avalonViewModel

@Composable
fun AlterarSenhaScreen(
    onBack: () -> Unit,
    onConcluido: () -> Unit,
    onContaBloqueada: () -> Unit,
    viewModel: AlterarSenhaViewModel = avalonViewModel { c, _ -> AlterarSenhaViewModel(c.authRepository) }
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.concluido) { if (state.concluido) onConcluido() }

    val voltar = if (state.etapa == EtapaSenha.CODIGO && !state.contaBloqueada) viewModel::voltar else onBack

    Column(Modifier.fillMaxSize().background(Background)) {
        AvalonTopBar("Alterar senha", onBack = voltar)
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).imePadding().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Esta troca altera somente a senha de acesso ao AVALON. A senha do e-mail corporativo só pode ser alterada pelo RH.",
                fontFamily = Inter, fontSize = 13.sp, color = TextSecondary
            )
            when {
                state.contaBloqueada -> {
                    state.error?.let { Banner(it) }
                    AvalonPrimaryButton("Voltar ao login", onClick = onContaBloqueada)
                }
                state.etapa == EtapaSenha.SENHA_ATUAL -> EtapaSenhaAtual(state, viewModel)
                else -> EtapaCodigo(state, viewModel)
            }
        }
    }
}

@Composable
private fun EtapaSenhaAtual(state: AlterarSenhaUiState, viewModel: AlterarSenhaViewModel) {
    AvalonCard(radius = 16.dp, border = BorderSoft, contentPadding = 15.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AvalonTextField(
                label = "Senha atual",
                value = state.senhaAtual,
                onValueChange = viewModel::onSenhaAtual,
                placeholder = "••••••••",
                isPassword = true,
                error = state.senhaAtualError,
                enabled = !state.enviandoCodigo,
                imeAction = ImeAction.Done,
                onImeAction = viewModel::enviarCodigo
            )
            Text(
                "Depois de confirmar a senha atual, enviaremos um código de $TAMANHO_CODIGO dígitos para o seu e-mail corporativo.",
                fontFamily = Inter, fontSize = 12.sp, color = TextSecondary
            )
        }
    }
    state.error?.let { Banner(it) }
    Spacer(Modifier.height(4.dp))
    AvalonPrimaryButton("Enviar código", onClick = viewModel::enviarCodigo, loading = state.enviandoCodigo)
}

@Composable
private fun EtapaCodigo(state: AlterarSenhaUiState, viewModel: AlterarSenhaViewModel) {
    Banner("Enviamos um código de $TAMANHO_CODIGO dígitos para o seu e-mail corporativo. Ele vale por 30 minutos.", destaque = false, erro = false)
    state.mensagem?.let { Banner(it, destaque = false, erro = false) }
    AvalonCard(radius = 16.dp, border = BorderSoft, contentPadding = 15.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AvalonTextField(
                label = "Código recebido",
                value = state.codigo,
                onValueChange = viewModel::onCodigo,
                placeholder = "0000",
                error = state.codigoError,
                enabled = !state.loading,
                keyboardType = KeyboardType.NumberPassword
            )
            AvalonTextField(
                label = "Nova senha",
                value = state.novaSenha,
                onValueChange = viewModel::onNovaSenha,
                placeholder = "••••••••",
                isPassword = true,
                error = state.novaSenhaError,
                enabled = !state.loading
            )
            AvalonTextField(
                label = "Confirme a nova senha",
                value = state.confirmacao,
                onValueChange = viewModel::onConfirmacao,
                placeholder = "••••••••",
                isPassword = true,
                error = state.confirmacaoError,
                enabled = !state.loading,
                imeAction = ImeAction.Done,
                onImeAction = viewModel::confirmar
            )
            Text(
                "Use pelo menos 8 caracteres, com letras e números. As outras sessões da sua conta serão encerradas.",
                fontFamily = Inter, fontSize = 12.sp, color = TextSecondary
            )
        }
    }
    state.error?.let { Banner(it) }
    Spacer(Modifier.height(4.dp))
    AvalonPrimaryButton("Salvar nova senha", onClick = viewModel::confirmar, loading = state.loading)
    val aguardando = state.segundosParaReenviar > 0
    TextButton(
        onClick = viewModel::reenviar,
        enabled = !aguardando && !state.enviandoCodigo && !state.loading,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            when {
                state.enviandoCodigo -> "Enviando…"
                aguardando -> "Reenviar código em ${CodigoRecuperacaoViewModel.formatarTempo(state.segundosParaReenviar)}"
                else -> "Reenviar código"
            },
            fontFamily = Inter, color = Navy
        )
    }
}
