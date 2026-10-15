package com.example.avalon_mobile.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.avalon_mobile.R
import com.example.avalon_mobile.ui.theme.Border
import com.example.avalon_mobile.ui.theme.Inter
import com.example.avalon_mobile.ui.theme.Navy
import com.example.avalon_mobile.ui.theme.TextPrimary
import com.example.avalon_mobile.ui.theme.TextSecondary

/** Estado de carregamento de uma tela. */
sealed interface LoadState<out T> {
    data object Loading : LoadState<Nothing>
    data class Error(val message: String) : LoadState<Nothing>
    data class Content<T>(val data: T) : LoadState<T>
}

/** Card branco com borda fina, padrão de listas do protótipo. */
@Composable
fun AvalonCard(
    modifier: Modifier = Modifier,
    radius: Dp = 14.dp,
    border: Color = Border,
    container: Color = Color.White,
    onClick: (() -> Unit)? = null,
    contentPadding: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(radius)
    val colors = CardDefaults.cardColors(containerColor = container)
    val stroke = BorderStroke(1.dp, border)
    if (onClick != null) {
        Card(onClick = onClick, modifier = modifier.fillMaxWidth(), shape = shape, colors = colors, border = stroke) {
            Column(Modifier.padding(contentPadding), content = content)
        }
    } else {
        Card(modifier = modifier.fillMaxWidth(), shape = shape, colors = colors, border = stroke) {
            Column(Modifier.padding(contentPadding), content = content)
        }
    }
}

/** Selo arredondado de status ("Em análise", "Aderido", "Anônima"...). */
@Composable
fun StatusPill(text: String, background: Color, foreground: Color, modifier: Modifier = Modifier, fontSize: Int = 12) {
    Box(
        modifier
            .background(background, RoundedCornerShape(30.dp))
            .padding(horizontal = 12.dp, vertical = 5.dp)
    ) {
        Text(text, color = foreground, fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = fontSize.sp)
    }
}

/** Ícone Lucide dentro de um quadrado com fundo suave (listas de documentos, notificações, benefícios). */
@Composable
fun IconTile(icon: Int, tint: Color, background: Color, modifier: Modifier = Modifier, size: Dp = 38.dp, iconSize: Dp = 20.dp) {
    Box(modifier.size(size).background(background, RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
        Icon(painterResource(icon), contentDescription = null, tint = tint, modifier = Modifier.size(iconSize))
    }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(text, modifier = modifier, fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
}

@Composable
fun LoadingBox(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = Navy)
    }
}

@Composable
fun ErrorBox(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(painterResource(R.drawable.ic_info), null, tint = TextSecondary, modifier = Modifier.size(32.dp))
        Text(message, modifier = Modifier.padding(top = 12.dp), textAlign = TextAlign.Center, fontFamily = Inter, fontSize = 15.sp, color = TextPrimary)
        TextButton(onClick = onRetry) { Text("Tentar novamente", fontFamily = Inter, color = Navy) }
    }
}

@Composable
fun EmptyState(message: String, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().padding(vertical = 40.dp), contentAlignment = Alignment.Center) {
        Text(message, fontFamily = Inter, fontSize = 15.sp, color = TextSecondary, textAlign = TextAlign.Center)
    }
}

/** Renderiza [LoadState] padronizando carregamento e erro. */
@Composable
fun <T> LoadStateContent(state: LoadState<T>, onRetry: () -> Unit, content: @Composable (T) -> Unit) {
    when (state) {
        LoadState.Loading -> LoadingBox()
        is LoadState.Error -> ErrorBox(state.message, onRetry)
        is LoadState.Content -> content(state.data)
    }
}

/** Snackbar global, fornecido pelo scaffold raiz da navegação. */
val LocalSnackbarHostState = androidx.compose.runtime.staticCompositionLocalOf { androidx.compose.material3.SnackbarHostState() }
