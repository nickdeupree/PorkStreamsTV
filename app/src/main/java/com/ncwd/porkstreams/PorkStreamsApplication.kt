package com.ncwd.porkstreams

import android.app.Application
import com.ncwd.porkstreams.data.local.AppContainer

class PorkStreamsApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
