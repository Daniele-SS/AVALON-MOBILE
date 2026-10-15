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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.avalon_mobile.R
import com.example.avalon_mobile.model.BeneficioItem
import com.example.avalon_mobile.ui.components.AvalonCard
import com.example.avalon_mobile.ui.components.AvalonTopBar
import com.example.avalon_mobile.ui.components.EmptyState
import com.example.avalon_mobile.ui.components.IconTile
import com.example.avalon_mobile.ui.components.LoadStateContent
import com.example.avalon_mobile.ui.components.LocalSnackbarHostState
import com.example.avalon_mobile.ui.components.StatusPill
import com.example.avalon_mobile.ui.theme.Background
import com.example.avalon_mobile.ui.theme.BadgeBlueBg
import com.example.avalon_mobile.ui.theme.BadgeBlueText
import com.example.avalon_mobile.ui.theme.BadgeGreenBg
import com.example.avalon_mobile.ui.theme.BadgeGreenText
import com.example.avalon_mobile.ui.theme.BorderSoft
import com.example.avalon_mobile.ui.theme.Danger
import com.example.avalon_mobile.ui.theme.Inter
import com.example.avalon_mobile.ui.theme.Navy
import com.example.avalon_mobile.ui.theme.TextSecondary
import com.example.avalon_mobile.ui.theme.TextStrong
import com.example.avalon_mobile.viewmodel.BeneficiosViewModel
import com.example.avalon_mobile.viewmodel.FiltroBeneficio
import com.example.avalon_mobile.viewmodel.avalonViewModel

@Composable
fun BeneficiosScreen(
    onBack: () -> Unit,
    viewModel: BeneficiosViewModel = avalonViewModel { c, _ -> BeneficiosViewModel(c.avalonRepository) }
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val filtro by viewModel.filtro.collectAsStateWithLifecycle()
    val snackbar = LocalSnackbarHostState.current
    LaunchedEffect(viewModel) { viewModel.mensagens.collect { snackbar.showSnackbar(it) } }

    Column(Modifier.fillMaxSize().background(Background)) {
        AvalonTopBar("Benefícios", subtitle = "Planos oferecidos pela empresa", onBack = onBack)
        Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FiltroBeneficio.entries.forEach { f ->
                FilterChip(
                    selected = f == filtro,
                    onClick = { viewModel.filtrar(f) },
                    label = { Text(f.rotulo, fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 13.sp) },
                    shape = RoundedCornerShape(20.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = Color.White,
                        labelColor = TextSecondary,
                        selectedContainerColor = Color(0xFF0B4F8A),
                        selectedLabelColor = Color.White
                    )
                )
            }
        }
        LoadStateContent(state, onRetry = viewModel::carregar) { itens ->
            val visiveis = itens.filter {
                when (filtro) {
                    FiltroBeneficio.TODOS -> true
                    FiltroBeneficio.ADERIDOS -> it.aderido
                    FiltroBeneficio.DISPONIVEIS -> !it.aderido
                }
            }
            LazyColumn(contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (visiveis.isEmpty()) item { EmptyState("Nenhum benefício nesta categoria.") }
                items(visiveis, key = { it.beneficio.id }) { BeneficioCard(it, onSolicitar = { viewModel.solicitar(it) }) }
            }
        }
    }
}

private fun iconeDo(nome: String): Int = with(nome.lowercase()) {
    when {
        "saúde" in this || "saude" in this || "odonto" in this -> R.drawable.ic_heart_pulse
        "aliment" in this || "refei" in this -> R.drawable.ic_star
        "seguro" in this -> R.drawable.ic_shield_check
        "gym" in this || "academia" in this -> R.drawable.ic_clock
        else -> R.drawable.ic_file_text
    }
}

@Composable
private fun BeneficioCard(item: BeneficioItem, onSolicitar: () -> Unit) {
    AvalonCard(radius = 14.dp, border = BorderSoft, contentPadding = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconTile(
                iconeDo(item.beneficio.nome),
                tint = if (item.aderido) BadgeGreenText else BadgeBlueText,
                background = if (item.aderido) BadgeGreenBg else BadgeBlueBg,
                size = 36.dp, iconSize = 20.dp
            )
            Spacer(Modifier.width(12.dp))
            Text(item.beneficio.nome, Modifier.weight(1f), fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextStrong)
            if (item.aderido) StatusPill("Aderido", BadgeGreenBg, BadgeGreenText, fontSize = 11)
            else StatusPill("Disponível", BadgeBlueBg, BadgeBlueText, fontSize = 11)
        }
        item.beneficio.descricao?.let {
            Spacer(Modifier.height(10.dp))
            Text(it, fontFamily = Inter, fontSize = 13.sp, color = TextSecondary)
        }
        Spacer(Modifier.height(6.dp))
        if (item.aderido) {
            TextButton(onClick = onSolicitar, contentPadding = PaddingValues(0.dp)) {
                Text("Solicitar Cancelamento", fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = Danger)
            }
        } else {
            Button(
                onClick = onSolicitar,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BadgeBlueBg, contentColor = Navy)
            ) { Text("Solicitar Adesão", fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = BadgeBlueText) }
        }
    }
}
