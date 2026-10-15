package com.example.avalon_mobile

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.avalon_mobile.navigation.AvalonNavHost
import com.example.avalon_mobile.ui.screens.debug.CrashScreen
import com.example.avalon_mobile.ui.theme.AVALONMOBILETheme
import com.example.avalon_mobile.util.CrashLog

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Cabeçalhos azul-marinho em todas as telas: ícones claros nas barras do sistema.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        val crashAnterior = if (BuildConfig.DEBUG) CrashLog.ler(this) else null
        setContent {
            var relatorio by remember { mutableStateOf(crashAnterior) }
            val atual = relatorio
            if (atual != null) {
                // Não toca no AppContainer: a tela de diagnóstico abre mesmo se ele for a causa.
                CrashScreen(atual, onContinuar = { CrashLog.limpar(this); relatorio = null })
            } else {
                AVALONMOBILETheme {
                    AvalonNavHost((application as AvalonApp).container)
                }
            }
        }
    }
}
