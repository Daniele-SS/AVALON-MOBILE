package com.example.avalon_mobile

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.avalon_mobile.navigation.AvalonNavHost
import com.example.avalon_mobile.ui.theme.AVALONMOBILETheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Cabeçalhos azul-marinho em todas as telas: ícones claros nas barras do sistema.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        val container = (application as AvalonApp).container
        setContent {
            AVALONMOBILETheme {
                AvalonNavHost(container)
            }
        }
    }
}
