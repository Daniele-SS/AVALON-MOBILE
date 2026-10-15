package com.example.avalon_mobile.util

import android.content.Context
import android.os.Build
import java.io.File

/**
 * Diagnóstico de build de debug: grava a última exceção não tratada para exibi-la
 * na próxima abertura do app, sem depender de logcat.
 */
object CrashLog {
    private const val ARQUIVO = "ultimo_crash.txt"

    fun instalar(context: Context) {
        val arquivo = File(context.filesDir, ARQUIVO)
        val anterior = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, erro ->
            runCatching {
                arquivo.writeText(
                    "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT}) • ${Build.MANUFACTURER} ${Build.MODEL}\n" +
                        "Thread: ${thread.name}\n\n" + erro.stackTraceToString()
                )
            }
            anterior?.uncaughtException(thread, erro)
        }
    }

    fun ler(context: Context): String? = File(context.filesDir, ARQUIVO).takeIf { it.exists() }?.readText()

    fun limpar(context: Context) {
        File(context.filesDir, ARQUIVO).delete()
    }
}
