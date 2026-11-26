package com.bplay.bhraspincasino.domain

import com.bplay.bhraspincasino.core.OfferEndpoint
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ResolveOfferLinkUseCaseTest {

    private val composer =
        OfferLinkComposer(OfferEndpoint(baseUrl = "https://host.test/path", source = "src"))

    @Test
    fun `link carries the advertising id, push token and click id`() = runTest {
        val resolve = ResolveOfferLinkUseCase(
            advertisingIds = FixedAdvertisingId("aaid-1"),
            pushTokens = FixedPushToken("token-1"),
            composer = composer,
        )

        assertEquals(
            "https://host.test/path?track_sr=src&track_gi=aaid-1&track_cl=pub-42&track_ph=token-1",
            resolve(clickId = "pub-42"),
        )
    }

    @Test
    fun `unavailable sources still produce a link`() = runTest {
        val resolve = ResolveOfferLinkUseCase(
            advertisingIds = FixedAdvertisingId(null),
            pushTokens = FixedPushToken(null),
            composer = composer,
        )

        assertEquals("https://host.test/path?track_sr=src&track_gi=&track_ph=", resolve())
    }

    private class FixedAdvertisingId(private val value: String?) : AdvertisingIdSource {
        override suspend fun advertisingId(): String? = value
    }

    private class FixedPushToken(private val value: String?) : PushTokenSource {
        override suspend fun pushToken(): String? = value
    }
}
