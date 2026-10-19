package com.example.avalon_mobile.ui.screens.perfil

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
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
import com.example.avalon_mobile.ui.theme.TextSecondary
import com.example.avalon_mobile.viewmodel.AlterarSenhaViewModel
import com.example.avalon_mobile.viewmodel.avalonViewModel

@Composable
fun AlterarSenhaScreen(
    onBack: () -> Unit,
    onConcluido: () -> Unit,
    viewModel: AlterarSenhaViewModel = avalonViewModel { c, _ -> AlterarSenhaViewModel(c.authRepository) }
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.concluido) { if (state.concluido) onConcluido() }

    Column(Modifier.fillMaxSize().background(Background)) {
        AvalonTopBar("Alterar senha", onBack = onBack)
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).imePadding().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AvalonCard(radius = 16.dp, border = BorderSoft, contentPadding = 15.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    AvalonTextField(
                        label = "Senha atual",
                        value = state.senhaAtual,
                        onValueChange = viewModel::onSenhaAtual,
                        placeholder = "••••••••",
                        isPassword = true,
                        error = state.senhaAtualError,
                        enabled = !state.loading
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
        }
    }
}
