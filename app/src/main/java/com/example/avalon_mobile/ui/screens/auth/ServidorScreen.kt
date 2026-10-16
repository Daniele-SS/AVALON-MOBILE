package com.example.avalon_mobile.ui.screens.auth

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.avalon_mobile.ui.components.AvalonPrimaryButton
import com.example.avalon_mobile.ui.components.AvalonSecondaryButton
import com.example.avalon_mobile.ui.components.AvalonTextField
import com.example.avalon_mobile.ui.theme.Inter
import com.example.avalon_mobile.ui.theme.Navy
import com.example.avalon_mobile.ui.theme.TextSecondary
import com.example.avalon_mobile.viewmodel.ServidorViewModel
import com.example.avalon_mobile.viewmodel.avalonViewModel

@Composable
fun ServidorScreen(
    onVoltar: () -> Unit,
    viewModel: ServidorViewModel = avalonViewModel { c, _ -> ServidorViewModel(c.apiConfig, c.servidorTester, c.sessionManager) }
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    AuthLayout(title = "Servidor da API", showTagline = false) {
        Text("Em uso: ${state.atual}", fontFamily = Inter, fontSize = 13.sp, color = TextSecondary)
        AvalonTextField(
            label = "Endereço da API",
            value = state.endereco,
            onValueChange = viewModel::onEndereco,
            placeholder = "http://192.168.0.10:8080/",
            error = state.enderecoError,
            enabled = !state.testando,
            keyboardType = KeyboardType.Uri,
            imeAction = ImeAction.Done,
            onImeAction = viewModel::testar
        )
        Text(
            "No emulador, use http://10.0.2.2:8080/. No celular, use o IP do computador que roda a API, na mesma rede Wi-Fi.",
            fontFamily = Inter, fontSize = 12.sp, color = TextSecondary
        )
        state.mensagem?.let { Banner(it, destaque = !state.sucesso, erro = !state.sucesso) }
        Spacer(Modifier.height(8.dp))
        AvalonSecondaryButton(if (state.testando) "Testando…" else "Testar conexão", onClick = viewModel::testar, modifier = Modifier.fillMaxWidth(), enabled = !state.testando)
        AvalonPrimaryButton("Salvar", onClick = viewModel::salvar, enabled = !state.testando)
        TextButton(onClick = viewModel::restaurarPadrao, enabled = !state.testando, modifier = Modifier.fillMaxWidth()) {
            Text("Restaurar padrão (${state.padrao})", fontFamily = Inter, color = Navy)
        }
        TextButton(onClick = onVoltar, modifier = Modifier.fillMaxWidth()) {
            Text("Voltar ao login", fontFamily = Inter, color = Navy)
        }
    }
}
