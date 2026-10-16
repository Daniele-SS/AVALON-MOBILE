package com.example.avalon_mobile.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.avalon_mobile.R
import com.example.avalon_mobile.ui.components.AvalonPrimaryButton
import com.example.avalon_mobile.ui.components.AvalonTextField
import com.example.avalon_mobile.ui.theme.TextSecondary
import com.example.avalon_mobile.ui.theme.Actor
import com.example.avalon_mobile.ui.theme.BadgeBlueBg
import com.example.avalon_mobile.ui.theme.Navy
import com.example.avalon_mobile.ui.theme.Danger
import com.example.avalon_mobile.ui.theme.DangerContainer
import com.example.avalon_mobile.ui.theme.Inter
import com.example.avalon_mobile.ui.theme.Link
import com.example.avalon_mobile.viewmodel.LoginEvent
import com.example.avalon_mobile.viewmodel.LoginViewModel
import com.example.avalon_mobile.viewmodel.avalonViewModel

/** Login com validação de status de conta (Sprint 1 — 6.2.1). */
@Composable
fun LoginScreen(
    onAutenticado: () -> Unit,
    onCodigoEnviado: (login: String) -> Unit,
    aviso: String? = null,
    servidor: String? = null,
    onServidor: () -> Unit = {},
    viewModel: LoginViewModel = avalonViewModel { c, _ -> LoginViewModel(c.authRepository) }
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                LoginEvent.Autenticado -> onAutenticado()
                is LoginEvent.CodigoEnviado -> onCodigoEnviado(event.login)
            }
        }
    }

    AuthLayout(title = "Acesse sua conta") {
        aviso?.let { Banner(it, destaque = false, erro = false) }
        // O contrato autentica por `login` (tbl_usuario.login); o protótipo rotula o campo como e-mail.
        AvalonTextField(
            label = "Login",
            value = state.login,
            onValueChange = viewModel::onLoginChange,
            placeholder = "maria.silva",
            error = state.loginError,
            enabled = !state.loading
        )
        AvalonTextField(
            label = "Senha",
            value = state.senha,
            onValueChange = viewModel::onSenhaChange,
            placeholder = "••••••••",
            isPassword = true,
            error = state.senhaError,
            enabled = !state.loading,
            imeAction = ImeAction.Done,
            onImeAction = viewModel::entrar
        )
        state.error?.let { Banner(it, destaque = state.contaInativa) }
        Spacer(Modifier.height(8.dp))
        AvalonPrimaryButton("Entrar", onClick = viewModel::entrar, loading = state.loading)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = viewModel::esqueciSenha, enabled = !state.loading && !state.enviandoRecuperacao) {
                Text(
                    if (state.enviandoRecuperacao) "Enviando…" else "Esqueci minha senha",
                    fontFamily = Actor, fontSize = 16.sp, color = Link,
                    textDecoration = TextDecoration.Underline
                )
            }
        }
        servidor?.let {
            TextButton(onClick = onServidor, enabled = !state.loading, modifier = Modifier.fillMaxWidth()) {
                Text("Servidor: $it", fontFamily = Actor, fontSize = 13.sp, color = TextSecondary)
            }
        }
    }
}

@Composable
internal fun Banner(text: String, destaque: Boolean = true, erro: Boolean = true) {
    val cor = if (erro) Danger else Navy
    Row(
        Modifier
            .fillMaxWidth()
            .background(if (erro) DangerContainer else BadgeBlueBg, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(painterResource(if (destaque) R.drawable.ic_shield_alert else R.drawable.ic_info), null, tint = cor, modifier = Modifier.size(18.dp))
        Box(Modifier.padding(start = 10.dp)) {
            Text(text, fontFamily = Inter, fontSize = 13.sp, color = cor)
        }
    }
}
