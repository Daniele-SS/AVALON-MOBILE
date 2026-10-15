package com.example.avalon_mobile

import android.app.Application
import com.example.avalon_mobile.data.AppContainer
import com.example.avalon_mobile.util.CrashLog

class AvalonApp : Application() {
    /** Criado sob demanda: uma falha aqui é capturada pelo CrashLog em vez de impedir a abertura. */
    val container: AppContainer by lazy { AppContainer(this) }

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) CrashLog.instalar(this)
    }
}
