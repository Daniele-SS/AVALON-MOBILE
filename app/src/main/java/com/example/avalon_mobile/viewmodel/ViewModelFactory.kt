package com.example.avalon_mobile.viewmodel

import androidx.compose.runtime.Composable
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

/** Cria ViewModels com acesso ao [AppContainer] e ao [SavedStateHandle] (argumentos de navegação). */
@Composable
inline fun <reified VM : ViewModel> avalonViewModel(
    crossinline create: (container: AppContainer, handle: SavedStateHandle) -> VM
): VM = viewModel(
    factory = viewModelFactory {
        initializer {
            val app = this[APPLICATION_KEY] as AvalonApp
            create(app.container, createSavedStateHandle())
        }
    }
)

fun <T> AppResult<T>.toLoadState(): LoadState<T> = when (this) {
    is AppResult.Success -> LoadState.Content(data)
    is AppResult.Failure -> LoadState.Error(error.mensagem)
}
