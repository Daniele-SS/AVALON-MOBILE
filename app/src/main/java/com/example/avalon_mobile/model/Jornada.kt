package com.example.avalon_mobile.model

import com.google.gson.annotations.SerializedName

data class JornadaEscala(
    val id: Int,
    val nome: String,
    val descricao: String? = null,
    @SerializedName("hora_inicio") val horaInicio: String,
    @SerializedName("hora_fim") val horaFim: String,
    val status: Boolean = true
)

data class DiaSemana(
    val id: Int,
    @SerializedName("dia_semana") val diaSemana: String,
    @SerializedName("dia_sigla") val diaSigla: String,
    val ativo: Boolean,
    @SerializedName("id_jornada_escala") val idJornadaEscala: Int? = null
)
