package com.example.avalon_mobile.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.avalon_mobile.ui.components.AvalonCard
import com.example.avalon_mobile.ui.components.AvalonTopBar
import com.example.avalon_mobile.ui.theme.Background
import com.example.avalon_mobile.ui.theme.Inter
import com.example.avalon_mobile.ui.theme.TextPrimary
import com.example.avalon_mobile.ui.theme.TextSecondary

enum class Informativo(val titulo: String, val secoes: List<Pair<String, String>>) {
    PRIVACIDADE(
        "Privacidade e Anonimato",
        listOf(
            "Seus dados (LGPD)" to "O AVALON trata seus dados pessoais apenas para as finalidades de gestão de pessoas previstas no contrato de trabalho e em obrigações legais. Você pode solicitar a correção de dados pelo Perfil.",
            "Pesquisas anônimas" to "Nas pesquisas e avaliações anônimas, suas respostas não são associadas à sua identidade em nenhum ponto do sistema. O envio é feito em conjunto e a confirmação não carrega identificadores.",
            "Resultados consolidados" to "Resultados só são exibidos quando o grupo atinge o número mínimo de respondentes (k-anonimato). Abaixo disso, nada é mostrado, nem para o RH.",
            "Avaliação psicossocial (NR-1)" to "A avaliação não gera diagnóstico nem classificação individual; os riscos são analisados por setor ou grupo.",
            "Sessão neste aparelho" to "O token de acesso é guardado cifrado com chave do Android Keystore e é removido ao sair da conta."
        )
    ),
    TERMOS(
        "Termos de Uso e Políticas",
        listOf(
            "Uso do aplicativo" to "O acesso é pessoal e intransferível. Não compartilhe sua senha. Ações relevantes são registradas em trilha de auditoria.",
            "Documentos" to "Documentos e holerites disponibilizados pelo RH são de uso pessoal. Arquivos baixados ficam no armazenamento privado do aplicativo.",
            "Canal de atendimento" to "Dúvidas sobre dados, benefícios ou políticas devem ser encaminhadas ao RH."
        )
    )
}

@Composable
fun InfoScreen(informativo: Informativo, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().background(Background)) {
        AvalonTopBar(informativo.titulo, onBack = onBack)
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            informativo.secoes.forEach { (titulo, texto) ->
                AvalonCard {
                    Text(titulo, fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                    Text(texto, Modifier.padding(top = 6.dp), fontFamily = Inter, fontSize = 13.sp, color = TextSecondary)
                }
            }
        }
    }
}
