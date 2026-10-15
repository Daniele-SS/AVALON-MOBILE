package com.example.avalon_mobile.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

/** Grava o PDF no cache privado e abre no visualizador do sistema via FileProvider. */
object DocumentOpener {

    fun open(context: Context, nomeArquivo: String, bytes: ByteArray): Boolean {
        val dir = File(context.cacheDir, "documentos").apply { mkdirs() }
        val seguro = nomeArquivo.replace(Regex("[^A-Za-z0-9._-]"), "_").take(80)
        val file = File(dir, if (seguro.endsWith(".pdf")) seguro else "$seguro.pdf")
        file.writeBytes(bytes)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_VIEW)
            .setDataAndType(uri, "application/pdf")
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            context.startActivity(Intent.createChooser(intent, "Abrir documento").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            true
        } catch (e: ActivityNotFoundException) {
            false
        }
    }
}
