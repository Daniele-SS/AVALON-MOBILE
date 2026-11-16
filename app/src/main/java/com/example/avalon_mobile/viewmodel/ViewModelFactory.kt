package com.example.avalon_mobile.viewmodel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.avalon_mobile.AvalonApp
import com.example.avalon_mobile.data.AppContainer
import com.example.avalon_mobile.data.AppResult
import com.example.avalon_mobile.ui.components.LoadState

/**
 * Chave do ViewModel: classe + usuário logado. A troca de abas guarda a tela e o ViewModel (saveState/restoreState) e o
 * logout não apaga esse estado; sem o usuário na chave, quem entra depois reaproveita a lista de quem saiu (achado do
 * TS108: a Juliana viu os documentos do Rafael). Com o usuário na chave, outro usuário sempre recebe ViewModels novos.
 */
fun chaveViewModel(classe: String, idColaborador: Int?): String = "$classe:usuario-${idColaborador ?: 0}"

/**
 * Cria ViewModels com acesso ao [AppContainer] e ao [SavedStateHandle] (argumentos de navegação). A chave é calculada
 * uma vez por composição (`remember`), para o ViewModel não ser trocado no meio da tela quando a sessão inicia (login).
 */
@Composable
inline fun <reified VM : ViewModel> avalonViewModel(
    crossinline create: (container: AppContainer, handle: SavedStateHandle) -> VM
): VM {
    val app = LocalContext.current.applicationContext as AvalonApp
    val chave = remember { chaveViewModel(VM::class.java.name, app.container.sessionManager.current?.usuario?.idColaborador) }
    return viewModel(
        key = chave,
        factory = viewModelFactory {
            initializer {
                val aplicacao = this[APPLICATION_KEY] as AvalonApp
                create(aplicacao.container, createSavedStateHandle())
            }
        }
    )
}

fun <T> AppResult<T>.toLoadState(): LoadState<T> = when (this) {
    is AppResult.Success -> LoadState.Content(data)
    is AppResult.Failure -> LoadState.Error(error.mensagem)
}
