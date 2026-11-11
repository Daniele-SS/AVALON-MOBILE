package com.example.avalon_mobile.util

import com.example.avalon_mobile.model.Pergunta

/** Regras puras da resposta de pesquisa (6.6.2): uma resposta em branco ou só com espaços não atende a obrigatoriedade. */
object PesquisaRegras {
    fun pendentes(perguntas: List<Pergunta>, respostas: Map<Int, String>): List<Pergunta> =
        perguntas.filter { it.obrigatorio && respostas[it.id].isNullOrBlank() }

    /** Posição da primeira obrigatória sem resposta, ou null quando não há pendência. */
    fun indiceDaPrimeiraPendente(perguntas: List<Pergunta>, respostas: Map<Int, String>): Int? =
        perguntas.indexOfFirst { it.obrigatorio && respostas[it.id].isNullOrBlank() }.takeIf { it >= 0 }
}
