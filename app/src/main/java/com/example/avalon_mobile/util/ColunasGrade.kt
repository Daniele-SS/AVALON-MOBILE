package com.example.avalon_mobile.util

/**
 * Escolhe quantas colunas uma grade de atalhos usa para que a palavra mais larga caiba inteira em cada card.
 * Com fonte maior (configuração do aparelho + "Tamanho da fonte" do app) a grade passa de 4 para 3, 2 ou 1
 * colunas em vez de cortar o texto. Todas as medidas na mesma unidade (px).
 */
object ColunasGrade {
    fun escolher(
        larguraDisponivel: Float,
        larguraMaiorPalavra: Float,
        paddingHorizontalItem: Float,
        espaco: Float,
        maximo: Int
    ): Int {
        for (colunas in maximo downTo 2) {
            val larguraCard = (larguraDisponivel - espaco * (colunas - 1)) / colunas
            if (larguraCard >= larguraMaiorPalavra + 2f * paddingHorizontalItem) return colunas
        }
        return 1
    }
}
