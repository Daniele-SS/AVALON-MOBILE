package com.example.avalon_mobile.model

import com.example.avalon_mobile.util.Datas
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

    /** Situação exibida na lista: sem data de disponibilização no contrato (tbl_documentos não tem a coluna), não se mostra "desde —". */
    val situacao: String
        get() = when {
            !ativo -> "Indisponível"
            Datas.parse(dataDisponibilizacao) != null -> "Disponível desde ${Datas.dataCurta(dataDisponibilizacao)}"
            else -> "Disponível"
        }

    /** Linha de apoio do tipo "Tipo • situação". */
    val legenda: String get() = "$tipoDocumento • $situacao"

    /** Legenda dos documentos assinados: a data só aparece quando existe; o formato do arquivo é sempre informado. */
    val legendaAssinado: String
        get() = Datas.parse(dataDisponibilizacao)?.let { "Assinado em ${Datas.dataCurta(dataDisponibilizacao)} • PDF" } ?: "Assinado • PDF"
}
