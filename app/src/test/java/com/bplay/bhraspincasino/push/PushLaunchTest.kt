package com.bplay.bhraspincasino.push

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PushLaunchTest {

    private fun parse(vararg pairs: Pair<String, String>): PushLaunch? =
        PushLaunch.from(mapOf(*pairs)::get)

    @Test
    fun `retention push carries only the click id`() {
        assertEquals(PushLaunch(clickId = "pub-42", offerUrl = null), parse("track_cl" to "pub-42"))
    }

    @Test
    fun `offer push carries its link`() {
        val url = "https://stay4play.online/offer?track_cl=pub-42"

        assertEquals(
            PushLaunch(clickId = "pub-42", offerUrl = url),
            parse("track_cl" to "pub-42", "url" to url)
        )
    }

    @Test
    fun `non web links are ignored`() {
        assertEquals(
            PushLaunch(clickId = "pub-42", offerUrl = null),
            parse("track_cl" to "pub-42", "url" to "javascript:alert(1)"),
        )
    }

    @Test
    fun `intent without push data is not a push launch`() {
        assertNull(parse())
        assertNull(parse("track_cl" to "  ", "url" to "market://details?id=x"))
    }
}
