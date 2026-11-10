package com.example.avalon_mobile.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.avalon_mobile.data.local.Aparencia
import com.example.avalon_mobile.data.local.AparenciaLocal
import com.example.avalon_mobile.data.local.EscalasFonte
import com.example.avalon_mobile.data.local.TemaApp

/** Estado observável da aparência: a troca é imediata (recompõe o app) e persiste no aparelho. */
class AparenciaState(private val local: AparenciaLocal) {
    var atual by mutableStateOf(local.ler())
        private set

    fun definirTema(tema: TemaApp) = atualizar(atual.copy(tema = tema))
    fun definirAltoContraste(ativo: Boolean) = atualizar(atual.copy(altoContraste = ativo))
    fun definirEscalaFonte(escala: Float) = atualizar(atual.copy(escalaFonte = EscalasFonte.normalizar(escala)))
    fun restaurarPadrao() = atualizar(Aparencia())

    private fun atualizar(nova: Aparencia) {
        atual = nova
        local.salvar(nova)
    }
}
