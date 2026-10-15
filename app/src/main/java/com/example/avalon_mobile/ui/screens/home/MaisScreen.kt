package com.example.avalon_mobile.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.avalon_mobile.BuildConfig
import com.example.avalon_mobile.R
import com.example.avalon_mobile.ui.components.AvalonTopBar
import com.example.avalon_mobile.ui.components.GradeFixa
import com.example.avalon_mobile.ui.theme.Background
import com.example.avalon_mobile.ui.theme.Danger
import com.example.avalon_mobile.ui.theme.DangerContainer
import com.example.avalon_mobile.ui.theme.Inter
import com.example.avalon_mobile.ui.theme.TextPrimary
import com.example.avalon_mobile.ui.theme.TextSecondary

enum class Modulo(val label: String, val icon: Int) {
    JORNADA("Jornada", R.drawable.ic_clock),
    NOTIFICACOES("Notificações", R.drawable.ic_bell),
    RISCOS("Riscos psicossociais", R.drawable.ic_shield),
    FERIAS("Férias", R.drawable.ic_umbrella_off),
    MURAL("Mural de avisos", R.drawable.ic_users),
    BENEFICIOS("Benefícios", R.drawable.ic_gift),
    DOWNLOADS("Downloads", R.drawable.ic_cloud_download),
    DOCUMENTOS("Documentos", R.drawable.ic_file),
    FEEDBACKS("Feedbacks", R.drawable.ic_message_circle),
    TERMOS("Termos e políticas", R.drawable.ic_book_open)
}

/** Todos os módulos do colaborador (Figma "Mais"). */
@Composable
fun MaisScreen(onBack: () -> Unit, onModulo: (Modulo) -> Unit, onSair: () -> Unit) {
    Column(Modifier.fillMaxSize().background(Background)) {
        AvalonTopBar("Mais", onBack = onBack)
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
            Text("Todos os módulos e serviços do colaborador.", fontFamily = Inter, fontSize = 13.sp, color = TextSecondary)
            Spacer(Modifier.height(16.dp))
            GradeFixa(Modulo.entries, colunas = 3, espaco = 12.dp, modifier = Modifier.fillMaxWidth()) { m ->
                    Card(
                        onClick = { onModulo(m) },
                        modifier = Modifier.weight(1f).height(86.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(Modifier.fillMaxSize().padding(6.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(painterResource(m.icon), null, tint = TextPrimary, modifier = Modifier.size(23.dp))
                            Spacer(Modifier.height(6.dp))
                            Text(m.label, fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = TextPrimary, textAlign = TextAlign.Center, lineHeight = 13.sp)
                        }
                    }
            }
            Spacer(Modifier.height(20.dp))
            Row(
                Modifier.fillMaxWidth().background(DangerContainer, RoundedCornerShape(8.dp)).clickable(onClick = onSair).padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(painterResource(R.drawable.ic_log_out), null, tint = Danger, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(12.dp))
                Text("Sair da Conta", Modifier.weight(1f), fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Danger)
                Icon(painterResource(R.drawable.ic_chevron_right), null, tint = TextSecondary, modifier = Modifier.size(16.dp))
            }
            Spacer(Modifier.height(16.dp))
            Text("Versão do aplicativo ${BuildConfig.VERSION_NAME}", Modifier.fillMaxWidth(), textAlign = TextAlign.Center, fontFamily = Inter, fontSize = 13.sp, color = TextSecondary)
        }
    }
}
