package com.bplay.bhraspincasino.core

import com.bplay.bhraspincasino.push.PushNotifier
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val pushModule = module {
    single { PushNotifier(androidContext()) }
}
