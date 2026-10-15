package com.example.avalon_mobile.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.avalon_mobile.R
import com.example.avalon_mobile.ui.theme.Actor
import com.example.avalon_mobile.ui.theme.BrunoAce
import com.example.avalon_mobile.ui.theme.Navy

/** Tela de abertura (Figma "Inicio"): esfera geodésica com o nome AVALON e ação "Entrar". */
@Composable
fun InicioScreen(onEntrar: () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Navy)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Box(Modifier.fillMaxWidth().align(Alignment.Center).padding(bottom = 60.dp), contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(R.drawable.avalon_sphere),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 480.dp)
                    .aspectRatio(1f)
                    .rotate(-18f)
            )
            Text("AVALON", fontFamily = BrunoAce, fontSize = 40.sp, color = Color.White, letterSpacing = 3.sp)
        }
        Column(Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp)) {
            TextButton(onClick = onEntrar) {
                Text("Entrar", fontFamily = Actor, fontSize = 32.sp, color = Color.White)
            }
        }
    }
}
