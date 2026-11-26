package com.bplay.bhraspincasino

import android.app.Application
import com.bplay.bhraspincasino.core.dataModule
import com.bplay.bhraspincasino.core.gateModule
import com.bplay.bhraspincasino.core.pushModule
import com.bplay.bhraspincasino.push.PushNotifier
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class BplayApp : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@BplayApp)
            modules(dataModule, pushModule, gateModule)
        }
        get<PushNotifier>().ensureChannel()
    }
}
