package com.example.avalon_mobile.util

import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.avalon_mobile.model.Colaborador
import java.io.ByteArrayOutputStream

/** Gera PDFs localmente: exportação do perfil (Downloads) e arquivos de demonstração. */
class PdfFactory {

    fun perfilCompleto(colaborador: Colaborador): ByteArray = pdf("Perfil do colaborador") { linha ->
        linha("Nome", colaborador.nome)
        linha("Matrícula", colaborador.matricula)
        linha("CPF", colaborador.cpfMascarado)
        linha("E-mail", colaborador.email)
        linha("Cargo", colaborador.cargoNome ?: "—")
        linha("Setor", colaborador.setorNome ?: "—")
        linha("GHE", colaborador.ghe ?: "—")
        linha("Admissão", Datas.dataCurta(colaborador.dataAdmissao))
        linha("Vínculo", colaborador.tipoVinculo ?: "—")
    }

    fun documentoDemonstracao(nome: String, tipo: String): ByteArray = pdf(nome) { linha ->
        linha("Tipo", tipo)
        linha("Origem", "Arquivo de demonstração (modo offline)")
    }

    private fun pdf(titulo: String, corpo: ((String, String) -> Unit) -> Unit): ByteArray {
        val document = PdfDocument()
        val page = document.startPage(PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, 1).create())
        val canvas = page.canvas
        val bold = Paint().apply { textSize = 18f; typeface = Typeface.DEFAULT_BOLD; isAntiAlias = true }
        val label = Paint().apply { textSize = 10f; color = 0xFF627D98.toInt(); isAntiAlias = true }
        val value = Paint().apply { textSize = 12f; color = 0xFF102A43.toInt(); isAntiAlias = true }
        canvas.drawText("AVALON", MARGIN, 48f, label)
        canvas.drawText(titulo, MARGIN, 72f, bold)
        var y = 110f
        corpo { rotulo, valor ->
            canvas.drawText(rotulo.uppercase(), MARGIN, y, label)
            canvas.drawText(valor, MARGIN, y + 16f, value)
            y += 40f
        }
        document.finishPage(page)
        return ByteArrayOutputStream().use { out ->
            document.writeTo(out)
            document.close()
            out.toByteArray()
        }
    }

    private companion object {
        const val PAGE_W = 595 // A4 em pontos
        const val PAGE_H = 842
        const val MARGIN = 48f
    }
}
