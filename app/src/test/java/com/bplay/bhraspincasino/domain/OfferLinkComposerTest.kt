package com.bplay.bhraspincasino.domain

import com.bplay.bhraspincasino.core.OfferEndpoint
import org.junit.Assert.assertEquals
import org.junit.Test

class OfferLinkComposerTest {

    private val composer =
        OfferLinkComposer(OfferEndpoint(baseUrl = "https://host.test/path", source = "src"))

    @Test
    fun `standard launch leaves track_cl out`() {
        val link = composer.compose(advertisingId = "aaid-1", clickId = null, pushToken = "token-1")

        assertEquals("https://host.test/path?track_sr=src&track_gi=aaid-1&track_ph=token-1", link)
    }

    @Test
    fun `push launch puts track_cl between track_gi and track_ph`() {
        val link =
            composer.compose(advertisingId = "aaid-1", clickId = "pub-42", pushToken = "token-1")

        assertEquals(
            "https://host.test/path?track_sr=src&track_gi=aaid-1&track_cl=pub-42&track_ph=token-1",
            link
        )
    }

    @Test
    fun `blank click id is treated as absent`() {
        val link = composer.compose(advertisingId = "aaid-1", clickId = " ", pushToken = "token-1")

        assertEquals("https://host.test/path?track_sr=src&track_gi=aaid-1&track_ph=token-1", link)
    }

    @Test
    fun `missing ids are sent empty and values are url encoded`() {
        val link =
            composer.compose(advertisingId = null, clickId = "a b", pushToken = "abc:APA91/x+y")

        assertEquals(
            "https://host.test/path?track_sr=src&track_gi=&track_cl=a+b&track_ph=abc%3AAPA91%2Fx%2By",
            link
        )
    }
}
