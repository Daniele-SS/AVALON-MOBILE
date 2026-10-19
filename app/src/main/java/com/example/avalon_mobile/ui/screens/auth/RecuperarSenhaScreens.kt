package com.example.avalon_mobile.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.avalon_mobile.ui.components.AvalonPrimaryButton
import com.example.avalon_mobile.ui.components.AvalonTextField
import com.example.avalon_mobile.ui.theme.Border
import com.example.avalon_mobile.ui.theme.Inter
import com.example.avalon_mobile.ui.theme.Navy
import com.example.avalon_mobile.ui.theme.TextPrimary
import com.example.avalon_mobile.ui.theme.TextSecondary
import com.example.avalon_mobile.viewmodel.CodigoRecuperacaoViewModel
import com.example.avalon_mobile.viewmodel.RedefinirSenhaViewModel
import com.example.avalon_mobile.viewmodel.TAMANHO_CODIGO
import com.example.avalon_mobile.viewmodel.avalonViewModel

/**
 * Recuperação de senha, etapa 1 (Sprint 2 — 6.2.2): código de 4 dígitos, de uso único e com validade de
 * 30 minutos, enviado ao e-mail corporativo. O reenvio fica bloqueado por 2 minutos (RF 5.1.84).
 * O código é o `token` de `/auth/senha/redefinir`; a validação acontece no servidor na etapa 2.
 */
@Composable
fun CodigoRecuperacaoScreen(
    onConfirmar: (codigo: String) -> Unit,
    onVoltar: () -> Unit,
    viewModel: CodigoRecuperacaoViewModel = avalonViewModel { c, h -> CodigoRecuperacaoViewModel(c.authRepository, h) }
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val codigo = state.codigo
    AuthLayout(title = "Código enviado para seu e-mail", showTagline = false) {
        Text(
            "Se a conta existir, você receberá um código de $TAMANHO_CODIGO dígitos no e-mail corporativo. Ele vale por 30 minutos.",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontFamily = Inter, fontSize = 13.sp, color = TextSecondary
        )
        BasicTextField(
            value = codigo,
            onValueChange = viewModel::onCodigo,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
            textStyle = TextStyle(fontFamily = Inter, fontSize = 20.sp, color = TextPrimary, letterSpacing = 10.sp, textAlign = TextAlign.Center),
            decorationBox = { inner ->
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .background(androidx.compose.ui.graphics.Color.White, RoundedCornerShape(8.dp))
                        .border(1.dp, Border, RoundedCornerShape(8.dp))
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (codigo.isEmpty()) {
                        Text(
                            "0 ".repeat(TAMANHO_CODIGO).trim(),
                            fontFamily = Inter, fontSize = 15.sp, color = TextSecondary, letterSpacing = 6.sp
                        )
                    }
                    inner()
                }
            }
        )
        state.mensagem?.let { Banner(it, destaque = false, erro = false) }
        state.erro?.let { Banner(it, destaque = state.contaBloqueada) }
        Spacer(Modifier.height(8.dp))
        AvalonPrimaryButton("Confirmar", onClick = { onConfirmar(codigo) }, enabled = codigo.length == TAMANHO_CODIGO && !state.contaBloqueada)
        val aguardando = state.segundosParaReenviar > 0
        TextButton(
            onClick = viewModel::reenviar,
            enabled = !aguardando && !state.reenviando && !state.contaBloqueada,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                when {
                    state.reenviando -> "Enviando…"
                    aguardando -> "Reenviar código em ${CodigoRecuperacaoViewModel.formatarTempo(state.segundosParaReenviar)}"
                    else -> "Reenviar código"
                },
                fontFamily = Inter, color = Navy
            )
        }
        TextButton(onClick = onVoltar, modifier = Modifier.fillMaxWidth()) {
            Text("Voltar ao login", fontFamily = Inter, color = Navy)
        }
    }
}

/** Recuperação de senha, etapa 2: nova senha + confirmação. */
@Composable
fun RedefinirSenhaScreen(
    onConcluido: () -> Unit,
    onCodigoInvalido: () -> Unit,
    viewModel: RedefinirSenhaViewModel = avalonViewModel { c, h -> RedefinirSenhaViewModel(c.authRepository, h) }
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.concluido) { if (state.concluido) onConcluido() }

    AuthLayout(title = "Redefina sua senha", showTagline = false) {
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
            label = "Confirme sua nova senha",
            value = state.confirmacao,
            onValueChange = viewModel::onConfirmacao,
            placeholder = "••••••••",
            isPassword = true,
            error = state.confirmacaoError,
            enabled = !state.loading,
            imeAction = ImeAction.Done,
            onImeAction = viewModel::confirmar
        )
        state.error?.let {
            Banner(it, destaque = state.contaBloqueada)
            if (!state.contaBloqueada) {
                TextButton(onClick = onCodigoInvalido) { Text("Informar outro código", fontFamily = Inter, color = Navy) }
            }
        }
        Spacer(Modifier.height(8.dp))
        AvalonPrimaryButton("Confirmar", onClick = viewModel::confirmar, loading = state.loading)
    }
}
