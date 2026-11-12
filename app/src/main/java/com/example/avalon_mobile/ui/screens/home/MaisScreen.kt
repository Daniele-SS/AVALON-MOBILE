package com.example.avalon_mobile.ui.screens.home

import androidx.compose.foundation.BorderStroke
import com.example.avalon_mobile.ui.theme.AtalhoFundo
import com.example.avalon_mobile.ui.theme.AtalhoConteudo
import com.example.avalon_mobile.ui.theme.Paletas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.avalon_mobile.ui.components.GradeAdaptavel
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.fillMaxHeight
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
import com.example.avalon_mobile.ui.theme.Background
import com.example.avalon_mobile.ui.theme.Surface
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
fun MaisScreen(onBack: () -> Unit, onModulo: (Modulo) -> Unit) {
    Column(Modifier.fillMaxSize().background(Background)) {
        AvalonTopBar("Mais", onBack = onBack)
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
            Text("Todos os módulos e serviços do colaborador.", fontFamily = Inter, fontSize = 13.sp, color = TextSecondary)
            Spacer(Modifier.height(16.dp))
            val estilo = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, lineHeight = 13.sp)
            val densidade = LocalDensity.current
            // Altura mínima e ícone acompanham o tamanho da fonte (sp→dp): o card cresce junto com as letras.
            val alturaMinima = maxOf(86.dp, with(densidade) { 86.sp.toDp() })
            val icone = maxOf(23.dp, with(densidade) { 23.sp.toDp() })
            GradeAdaptavel(
                itens = Modulo.entries, colunasMaximas = 3, espaco = 12.dp,
                rotulo = { it.label }, estiloRotulo = estilo, paddingHorizontalItem = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) { m ->
                Card(
                    onClick = { onModulo(m) },
                    modifier = Modifier.weight(1f).fillMaxHeight().heightIn(min = alturaMinima),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = AtalhoFundo),
                    border = if (Paletas.altoContraste) BorderStroke(2.dp, Color.Black) else null
                ) {
                    Column(Modifier.fillMaxSize().padding(6.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(painterResource(m.icon), null, tint = AtalhoConteudo, modifier = Modifier.size(icone))
                        Spacer(Modifier.height(6.dp))
                        Text(m.label, style = estilo, color = AtalhoConteudo, textAlign = TextAlign.Center)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Text("Versão do aplicativo ${BuildConfig.VERSION_NAME}", Modifier.fillMaxWidth(), textAlign = TextAlign.Center, fontFamily = Inter, fontSize = 13.sp, color = TextSecondary)
        }
    }
}
