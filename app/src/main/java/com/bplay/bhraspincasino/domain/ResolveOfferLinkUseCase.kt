package com.bplay.bhraspincasino.domain

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class ResolveOfferLinkUseCase(
    private val advertisingIds: AdvertisingIdSource,
    private val pushTokens: PushTokenSource,
    private val composer: OfferLinkComposer,
) {

    suspend operator fun invoke(clickId: String? = null): String = coroutineScope {
        val advertisingId = async { advertisingIds.advertisingId() }
        val pushToken = async { pushTokens.pushToken() }
        composer.compose(
            advertisingId = advertisingId.await(),
            clickId = clickId,
            pushToken = pushToken.await(),
        )
    }
}
