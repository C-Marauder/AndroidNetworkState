package com.example.networkstate

import android.app.Application
import com.xhh.networkstate.NetworkStateManager

class App:Application() {

    override fun onCreate() {
        super.onCreate()
        NetworkStateManager.init(this)
    }
}