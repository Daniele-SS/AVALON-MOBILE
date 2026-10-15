package com.example.avalon_mobile.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.avalon_mobile.R
import com.example.avalon_mobile.ui.theme.Inter
import com.example.avalon_mobile.ui.theme.Navy
import com.example.avalon_mobile.ui.theme.TabInactive

/** Cabeçalho azul-marinho das telas internas (voltar + título + subtítulo opcional). */
@Composable
fun AvalonTopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    backIcon: Int = R.drawable.ic_arrow_left,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Navy)
            .statusBarsPadding()
            .padding(start = if (onBack != null) 12.dp else 24.dp, end = 12.dp, top = 12.dp, bottom = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(painterResource(backIcon), contentDescription = "Voltar", tint = Color.White, modifier = Modifier.size(28.dp))
            }
            Spacer(Modifier.width(4.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(title, color = Color.White, fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
            if (subtitle != null) {
                Text(subtitle, color = Color.White.copy(alpha = 0.6f), fontFamily = Inter, fontSize = 13.sp)
            }
        }
        actions()
    }
}

enum class MainTab(val label: String, @DrawableRes val icon: Int) {
    HOME("Home", R.drawable.ic_house),
    PESQUISAS("Pesquisas", R.drawable.ic_clipboard_list),
    DOCUMENTOS("Documentos", R.drawable.ic_file_text),
    PERFIL("Perfil", R.drawable.ic_user_round)
}

/** Barra inferior com as quatro abas do protótipo. */
@Composable
fun AvalonBottomBar(selected: MainTab?, onSelect: (MainTab) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Navy)
            .navigationBarsPadding()
            .height(64.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        MainTab.entries.forEach { tab ->
            val isSelected = tab == selected
            val color = if (isSelected) Color.White else TabInactive
            Column(
                modifier = Modifier
                    .weight(1f)
                    .semantics { this.selected = isSelected }
                    .clickable(role = Role.Tab) { onSelect(tab) }
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(painterResource(tab.icon), contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                Spacer(Modifier.height(4.dp))
                Text(
                    tab.label,
                    color = color,
                    fontFamily = Inter,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 11.sp
                )
            }
        }
    }
}
