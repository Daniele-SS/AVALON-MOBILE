package com.example.avalon_mobile.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.avalon_mobile.R
import com.example.avalon_mobile.ui.theme.BrunoAce

/** Logotipo "AVAL◎N": a letra O é a esfera geodésica, como no protótipo. */
@Composable
fun AvalonWordmark(
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 36.sp,
    color: Color = Color.White
) {
    val style = TextStyle(fontFamily = BrunoAce, fontSize = fontSize, color = color, letterSpacing = (fontSize.value * 0.08f).sp)
    Row(
        modifier = modifier.semantics(mergeDescendants = true) { contentDescription = "AVALON" },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("AVAL", style = style)
        Spacer(Modifier.width((fontSize.value * 0.05f).dp))
        Image(
            painter = painterResource(R.drawable.avalon_sphere),
            contentDescription = null,
            modifier = Modifier.size((fontSize.value * 0.95f).dp)
        )
        Spacer(Modifier.width((fontSize.value * 0.08f).dp))
        Text("N", style = style)
    }
}
