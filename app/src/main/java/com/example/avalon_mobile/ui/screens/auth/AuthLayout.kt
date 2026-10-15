package com.example.avalon_mobile.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.avalon_mobile.ui.components.AvalonWordmark
import com.example.avalon_mobile.ui.theme.Actor
import com.example.avalon_mobile.ui.theme.Navy
import com.example.avalon_mobile.ui.theme.TextSubtle

/** Estrutura comum das telas de acesso: logotipo no topo e folha branca arredondada. */
@Composable
fun AuthLayout(
    title: String,
    showTagline: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Navy)
            .statusBarsPadding()
    ) {
        Box(Modifier.fillMaxWidth().height(230.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AvalonWordmark(fontSize = 36.sp)
                if (showTagline) {
                    Text(
                        "Gestão e Experiência do Colaborador",
                        modifier = Modifier.padding(top = 8.dp),
                        fontFamily = Actor, fontSize = 14.sp, color = TextSubtle
                    )
                }
            }
        }
        Column(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.White, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                title,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontFamily = Actor, fontSize = 26.sp, color = Navy
            )
            content()
        }
    }
}
