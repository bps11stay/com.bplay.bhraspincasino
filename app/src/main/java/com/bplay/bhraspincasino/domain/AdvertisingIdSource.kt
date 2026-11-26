package com.bplay.bhraspincasino.domain

interface AdvertisingIdSource {
    suspend fun advertisingId(): String?
}
