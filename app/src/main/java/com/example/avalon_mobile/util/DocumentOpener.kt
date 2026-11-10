package com.example.avalon_mobile.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

/** Tipo do arquivo, lido dos primeiros bytes: o servidor entrega o que o RH enviou (PDF, imagem ou outro). */
data class TipoArquivo(val mime: String, val extensao: String)

/** Grava o arquivo no cache privado e abre no visualizador do sistema via FileProvider. */
object DocumentOpener {

    fun tipoPorConteudo(bytes: ByteArray): TipoArquivo = when {
        bytes.startsWith(byteArrayOf(0x25, 0x50, 0x44, 0x46)) -> TipoArquivo("application/pdf", ".pdf")
        bytes.startsWith(byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47)) -> TipoArquivo("image/png", ".png")
        bytes.startsWith(byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte())) -> TipoArquivo("image/jpeg", ".jpg")
        else -> TipoArquivo("application/pdf", ".pdf")
    }

    private fun ByteArray.startsWith(prefixo: ByteArray) =
        size >= prefixo.size && prefixo.indices.all { this[it] == prefixo[it] }

    fun open(context: Context, nomeArquivo: String, bytes: ByteArray): Boolean {
        val dir = File(context.cacheDir, "documentos").apply { mkdirs() }
        val seguro = nomeArquivo.replace(Regex("[^A-Za-z0-9._-]"), "_").take(80)
        val tipo = tipoPorConteudo(bytes)
        val file = File(dir, if (seguro.endsWith(tipo.extensao)) seguro else "${seguro.substringBeforeLast('.', seguro)}${tipo.extensao}")
        file.writeBytes(bytes)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_VIEW)
            .setDataAndType(uri, tipo.mime)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            context.startActivity(Intent.createChooser(intent, "Abrir documento").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            true
        } catch (e: ActivityNotFoundException) {
            false
        }
    }
}
