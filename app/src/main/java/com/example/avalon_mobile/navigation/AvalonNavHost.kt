package com.example.avalon_mobile.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.avalon_mobile.BuildConfig
import com.example.avalon_mobile.data.AppContainer
import com.example.avalon_mobile.ui.components.AvalonBottomBar
import com.example.avalon_mobile.ui.components.LocalSnackbarHostState
import com.example.avalon_mobile.ui.components.MainTab
import com.example.avalon_mobile.ui.screens.auth.CodigoRecuperacaoScreen
import com.example.avalon_mobile.ui.screens.auth.InicioScreen
import com.example.avalon_mobile.ui.screens.auth.LoginScreen
import com.example.avalon_mobile.ui.screens.auth.RedefinirSenhaScreen
import com.example.avalon_mobile.ui.screens.auth.ServidorScreen
import com.example.avalon_mobile.ui.screens.conteudo.BeneficiosScreen
import com.example.avalon_mobile.ui.screens.conteudo.DocumentosScreen
import com.example.avalon_mobile.ui.screens.conteudo.DownloadsScreen
import com.example.avalon_mobile.ui.screens.feedback.FeedbackDetalheScreen
import com.example.avalon_mobile.ui.screens.feedback.FeedbacksScreen
import com.example.avalon_mobile.ui.screens.ferias.FeriasScreen
import com.example.avalon_mobile.ui.screens.home.Atalho
import com.example.avalon_mobile.ui.screens.home.ComunicadosScreen
import com.example.avalon_mobile.ui.screens.home.HomeScreen
import com.example.avalon_mobile.ui.screens.home.InfoScreen
import com.example.avalon_mobile.ui.screens.home.Informativo
import com.example.avalon_mobile.ui.screens.home.MaisScreen
import com.example.avalon_mobile.ui.screens.home.Modulo
import com.example.avalon_mobile.ui.screens.home.NotificacoesScreen
import com.example.avalon_mobile.ui.screens.perfil.AlterarSenhaScreen
import com.example.avalon_mobile.ui.screens.perfil.JornadaScreen
import com.example.avalon_mobile.ui.screens.perfil.PerfilScreen
import com.example.avalon_mobile.ui.screens.pesquisa.AvaliacaoScreen
import com.example.avalon_mobile.ui.screens.pesquisa.PesquisaScreen
import com.example.avalon_mobile.ui.screens.pesquisa.PesquisasScreen
import kotlinx.coroutines.launch

object Routes {
    const val INICIO = "inicio"
    const val LOGIN = "login"
    const val CODIGO = "recuperar/codigo/{login}"
    const val REDEFINIR = "recuperar/redefinir/{token}"
    const val SERVIDOR = "servidor"
    const val HOME = "home"
    const val PESQUISAS = "pesquisas"
    const val DOCUMENTOS = "documentos"
    const val PERFIL = "perfil"
    const val JORNADA = "jornada"
    const val ALTERAR_SENHA = "perfil/senha"
    const val FERIAS = "ferias"
    const val BENEFICIOS = "beneficios"
    const val DOWNLOADS = "downloads"
    const val FEEDBACKS = "feedbacks"
    const val FEEDBACK = "feedback/{id}"
    const val PESQUISA = "pesquisa/{id}"
    const val AVALIACAO = "avaliacao"
    const val NOTIFICACOES = "notificacoes"
    const val COMUNICADOS = "comunicados"
    const val MAIS = "mais"
    const val PRIVACIDADE = "privacidade"
    const val TERMOS = "termos"

    fun codigo(login: String) = "recuperar/codigo/${android.net.Uri.encode(login)}"
    fun redefinir(token: String) = "recuperar/redefinir/$token"
    fun feedback(id: Int) = "feedback/$id"
    fun pesquisa(id: Int) = "pesquisa/$id"
}

/** Rotas que exibem a barra inferior e a aba destacada em cada uma. */
private val abaPorRota = mapOf(
    Routes.HOME to MainTab.HOME,
    Routes.PESQUISAS to MainTab.PESQUISAS,
    Routes.DOCUMENTOS to MainTab.DOCUMENTOS,
    Routes.PERFIL to MainTab.PERFIL,
    Routes.DOWNLOADS to MainTab.DOCUMENTOS,
    Routes.JORNADA to MainTab.PERFIL,
    Routes.ALTERAR_SENHA to MainTab.PERFIL,
    Routes.FERIAS to MainTab.HOME,
    Routes.BENEFICIOS to MainTab.HOME,
    Routes.FEEDBACKS to MainTab.HOME,
    Routes.NOTIFICACOES to MainTab.HOME,
    Routes.COMUNICADOS to MainTab.HOME,
    Routes.MAIS to MainTab.HOME,
    Routes.PRIVACIDADE to MainTab.PERFIL,
    Routes.TERMOS to MainTab.PERFIL
)

private fun MainTab.route() = when (this) {
    MainTab.HOME -> Routes.HOME
    MainTab.PESQUISAS -> Routes.PESQUISAS
    MainTab.DOCUMENTOS -> Routes.DOCUMENTOS
    MainTab.PERFIL -> Routes.PERFIL
}

/** Troca de aba preservando o estado de cada uma; após o login a raiz da pilha é sempre a Home. */
private fun NavHostController.irParaAba(route: String) = navigate(route) {
    popUpTo(Routes.HOME) { saveState = true }
    launchSingleTop = true
    restoreState = true
}

/** Limpa a pilha inteira (login/logout/sessão expirada). */
private fun NavHostController.reiniciarEm(route: String) = navigate(route) {
    popUpTo(graph.id) { inclusive = true }
    launchSingleTop = true
}

@Composable
fun AvalonNavHost(container: AppContainer) {
    val navController = rememberNavController()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val backStack by navController.currentBackStackEntryAsState()
    val rotaAtual = backStack?.destination?.route
    var avisoLogin by rememberSaveable { mutableStateOf<String?>(null) }
    val servidorAtual by container.apiConfig.baseUrl.collectAsState()
    val inicio = remember { if (container.sessionManager.current != null) Routes.HOME else Routes.INICIO }

    // 6.2.3: refresh negado ou conta inativada durante o uso → volta ao login.
    LaunchedEffect(container) {
        container.sessionManager.expired.collect {
            avisoLogin = "Sua sessão expirou. Faça login novamente."
            navController.reiniciarEm(Routes.LOGIN)
        }
    }

    val sair: () -> Unit = {
        scope.launch {
            container.authRepository.logout()
            avisoLogin = null
            navController.reiniciarEm(Routes.LOGIN)
        }
    }

    fun abrir(route: String) = navController.navigate(route) { launchSingleTop = true }

    CompositionLocalProvider(LocalSnackbarHostState provides snackbar) {
        Scaffold(
            contentWindowInsets = WindowInsets(0),
            snackbarHost = { SnackbarHost(snackbar) },
            bottomBar = {
                abaPorRota[rotaAtual]?.let { aba ->
                    AvalonBottomBar(selected = aba, onSelect = { navController.irParaAba(it.route()) })
                }
            }
        ) { padding ->
            NavHost(navController, startDestination = inicio, modifier = Modifier.padding(padding)) {
                // Sprint 1 — 6.1 / 6.2.1
                composable(Routes.INICIO) { InicioScreen(onEntrar = { abrir(Routes.LOGIN) }) }
                composable(Routes.LOGIN) {
                    LoginScreen(
                        aviso = avisoLogin,
                        servidor = if (BuildConfig.DEBUG) "${servidorAtual.host}:${servidorAtual.port}" else null,
                        onServidor = { abrir(Routes.SERVIDOR) },
                        onAutenticado = { avisoLogin = null; navController.reiniciarEm(Routes.HOME) },
                        onCodigoEnviado = { login -> abrir(Routes.codigo(login)) }
                    )
                }
                composable(Routes.SERVIDOR) { ServidorScreen(onVoltar = { navController.popBackStack() }) }
                // Sprint 2 — 6.2.2
                composable(Routes.CODIGO, arguments = listOf(navArgument("login") { type = NavType.StringType })) {
                    CodigoRecuperacaoScreen(
                        onConfirmar = { codigo -> abrir(Routes.redefinir(codigo)) },
                        onVoltar = { navController.popBackStack() }
                    )
                }
                composable(Routes.REDEFINIR, arguments = listOf(navArgument("token") { type = NavType.StringType })) {
                    RedefinirSenhaScreen(
                        onConcluido = {
                            avisoLogin = "Senha redefinida. Entre com a nova senha."
                            navController.navigate(Routes.LOGIN) { popUpTo(Routes.LOGIN) { inclusive = true } }
                        },
                        onCodigoInvalido = { navController.popBackStack() }
                    )
                }

                // Abas principais
                composable(Routes.HOME) {
                    HomeScreen(
                        onAtalho = { atalho ->
                            when (atalho) {
                                Atalho.JORNADA -> abrir(Routes.JORNADA)
                                Atalho.FERIAS -> abrir(Routes.FERIAS)
                                Atalho.BENEFICIOS -> abrir(Routes.BENEFICIOS)
                                Atalho.DOCUMENTOS -> navController.irParaAba(Routes.DOCUMENTOS)
                                Atalho.FEEDBACKS -> abrir(Routes.FEEDBACKS)
                                Atalho.PESQUISAS -> navController.irParaAba(Routes.PESQUISAS)
                                Atalho.MAIS -> abrir(Routes.MAIS)
                            }
                        },
                        onNotificacoes = { abrir(Routes.NOTIFICACOES) },
                        onComunicados = { abrir(Routes.COMUNICADOS) }
                    )
                }
                composable(Routes.PESQUISAS) {
                    PesquisasScreen(
                        onBack = null,
                        onAbrirPesquisa = { abrir(Routes.pesquisa(it)) },
                        onAbrirAvaliacao = { abrir(Routes.AVALIACAO) }
                    )
                }
                composable(Routes.DOCUMENTOS) { DocumentosScreen(onBack = null) }
                composable(Routes.PERFIL) {
                    PerfilScreen(
                        onPrivacidade = { abrir(Routes.PRIVACIDADE) },
                        onNotificacoes = { abrir(Routes.NOTIFICACOES) },
                        onTermos = { abrir(Routes.TERMOS) },
                        onAlterarSenha = { abrir(Routes.ALTERAR_SENHA) },
                        onSair = { avisoLogin = null; navController.reiniciarEm(Routes.LOGIN) }
                    )
                }

                // Sprint 2 — 6.5.3
                composable(Routes.ALTERAR_SENHA) {
                    AlterarSenhaScreen(
                        onBack = { navController.popBackStack() },
                        onConcluido = {
                            navController.popBackStack()
                            scope.launch { snackbar.showSnackbar("Senha alterada com sucesso.") }
                        },
                        onContaBloqueada = {
                            scope.launch {
                                container.authRepository.logout()
                                avisoLogin = "Conta bloqueada por excesso de tentativas. Procure o RH para redefinir sua senha."
                                navController.reiniciarEm(Routes.LOGIN)
                            }
                        }
                    )
                }
                composable(Routes.JORNADA) { JornadaScreen(onBack = { navController.popBackStack() }) }
                // Sprint 3 — 6.4, 6.5.4, 6.7
                composable(Routes.FERIAS) { FeriasScreen(onBack = { navController.popBackStack() }) }
                composable(Routes.BENEFICIOS) { BeneficiosScreen(onBack = { navController.popBackStack() }) }
                composable(Routes.DOWNLOADS) { DownloadsScreen(onBack = { navController.popBackStack() }) }
                composable(Routes.FEEDBACKS) {
                    FeedbacksScreen(onBack = { navController.popBackStack() }, onAbrir = { abrir(Routes.feedback(it)) })
                }
                composable(Routes.FEEDBACK, arguments = listOf(navArgument("id") { type = NavType.IntType })) {
                    FeedbackDetalheScreen(onBack = { navController.popBackStack() })
                }
                // Sprint 4 — 6.6, 6.3.x
                composable(Routes.PESQUISA, arguments = listOf(navArgument("id") { type = NavType.IntType })) {
                    PesquisaScreen(onSair = { navController.popBackStack() })
                }
                composable(Routes.AVALIACAO) { AvaliacaoScreen(onSair = { navController.popBackStack() }) }
                composable(Routes.NOTIFICACOES) {
                    NotificacoesScreen(
                        onBack = { navController.popBackStack() },
                        onAbrirOrigem = { tipo ->
                            when (tipo?.uppercase()) {
                                "PESQUISA" -> navController.irParaAba(Routes.PESQUISAS)
                                "AVALIACAO" -> abrir(Routes.AVALIACAO)
                                "FERIAS" -> abrir(Routes.FERIAS)
                                "FEEDBACK" -> abrir(Routes.FEEDBACKS)
                                "BENEFICIO" -> abrir(Routes.BENEFICIOS)
                                "DOCUMENTO" -> navController.irParaAba(Routes.DOCUMENTOS)
                            }
                        }
                    )
                }
                composable(Routes.COMUNICADOS) { ComunicadosScreen(onBack = { navController.popBackStack() }) }
                composable(Routes.MAIS) {
                    MaisScreen(
                        onBack = { navController.popBackStack() },
                        onSair = sair,
                        onModulo = { m ->
                            when (m) {
                                Modulo.JORNADA -> abrir(Routes.JORNADA)
                                Modulo.NOTIFICACOES -> abrir(Routes.NOTIFICACOES)
                                Modulo.RISCOS -> abrir(Routes.AVALIACAO)
                                Modulo.FERIAS -> abrir(Routes.FERIAS)
                                Modulo.MURAL -> abrir(Routes.COMUNICADOS)
                                Modulo.BENEFICIOS -> abrir(Routes.BENEFICIOS)
                                Modulo.DOWNLOADS -> abrir(Routes.DOWNLOADS)
                                Modulo.DOCUMENTOS -> navController.irParaAba(Routes.DOCUMENTOS)
                                Modulo.FEEDBACKS -> abrir(Routes.FEEDBACKS)
                                Modulo.TERMOS -> abrir(Routes.TERMOS)
                            }
                        }
                    )
                }
                composable(Routes.PRIVACIDADE) { InfoScreen(Informativo.PRIVACIDADE, onBack = { navController.popBackStack() }) }
                composable(Routes.TERMOS) { InfoScreen(Informativo.TERMOS, onBack = { navController.popBackStack() }) }
            }
        }
    }
}
