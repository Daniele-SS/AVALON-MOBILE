package com.example.avalon_mobile.model

import com.google.gson.annotations.SerializedName

data class Beneficio(
    val id: Int,
    val nome: String,
    val descricao: String? = null,
    val status: Boolean = true,
    val disponibilidade: String = "TODOS"
)

/** Concessão de benefício ao colaborador (`BeneficioColaborador`). */
data class BeneficioColaborador(
    val id: Int,
    @SerializedName("id_colaborador") val idColaborador: Int,
    @SerializedName("id_beneficio") val idBeneficio: Int? = null,
    @SerializedName("data_inicio") val dataInicio: String,
    @SerializedName("data_fim") val dataFim: String? = null,
    val ativo: Boolean = true,
    val beneficio: Beneficio? = null
)

/** Item exibido na tela de Benefícios: aderido (concessão ativa) ou apenas disponível. */
data class BeneficioItem(
    val beneficio: Beneficio,
    val aderido: Boolean
)
