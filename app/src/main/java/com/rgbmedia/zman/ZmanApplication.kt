package com.rgbmedia.zman

import android.app.Application

class ZmanApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        instance = this
    }

    companion object {
        lateinit var instance: ZmanApplication
            private set
    }
}
