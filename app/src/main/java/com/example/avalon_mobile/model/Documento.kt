package com.example.avalon_mobile.model

import com.google.gson.annotations.SerializedName

data class Documento(
    val id: Int,
    @SerializedName("id_colaborador") val idColaborador: Int,
    val nome: String,
    @SerializedName("tipo_documento") val tipoDocumento: String,
    @SerializedName("caminho_arquivo") val caminhoArquivo: String? = null,
    val ativo: Boolean = true,
    // Campos de apresentação do protótipo ainda ausentes no contrato.
    @SerializedName("data_disponibilizacao") val dataDisponibilizacao: String? = null,
    @SerializedName("tamanho_bytes") val tamanhoBytes: Long? = null,
    val lido: Boolean? = null
) {
    /** Contratos e termos assinados x extratos/holerites (RF 1.1.x Documentos / 6.5.4). */
    val assinado: Boolean
        get() = tipoDocumento in setOf("Contrato", "Termo", "Aditivo Contratual", "Política")
}
