package com.bplay.bhraspincasino.core

import com.bplay.bhraspincasino.data.FirebasePushTokenSource
import com.bplay.bhraspincasino.data.PlayAdvertisingIdSource
import com.bplay.bhraspincasino.data.PushTokenStore
import com.bplay.bhraspincasino.domain.AdvertisingIdSource
import com.bplay.bhraspincasino.domain.OfferLinkComposer
import com.bplay.bhraspincasino.domain.PushTokenSource
import com.bplay.bhraspincasino.domain.ResolveOfferLinkUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataModule = module {
    single { OfferEndpoint.Default }
    single { CoroutineScope(SupervisorJob() + Dispatchers.IO) }
    single { PushTokenStore(androidContext()) }
    single<AdvertisingIdSource> { PlayAdvertisingIdSource(androidContext()) }
    single<PushTokenSource> { FirebasePushTokenSource(get(), get()) }
    factory { OfferLinkComposer(get()) }
    factory { ResolveOfferLinkUseCase(get(), get(), get()) }
}
