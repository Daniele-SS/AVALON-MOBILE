package com.example.avalon_mobile

import android.app.Application
import com.example.avalon_mobile.data.AppContainer

class AvalonApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
