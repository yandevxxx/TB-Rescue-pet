package com.yarsi.rescuepet

import android.app.Application
import com.yarsi.rescuepet.data.remote.AppwriteClient

class AppwriteApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppwriteClient.initialize(this)
    }
}
