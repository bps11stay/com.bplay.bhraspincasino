package com.bplay.bhraspincasino.data

import android.content.Context
import com.bplay.bhraspincasino.domain.AdvertisingIdSource
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PlayAdvertisingIdSource(private val context: Context) : AdvertisingIdSource {

    override suspend fun advertisingId(): String? = withContext(Dispatchers.IO) {
        runCatching { AdvertisingIdClient.getAdvertisingIdInfo(context).id }.getOrNull()
    }
}
