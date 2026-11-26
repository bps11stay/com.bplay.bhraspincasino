package com.bplay.bhraspincasino.domain

import com.bplay.bhraspincasino.core.OfferEndpoint
import java.net.URLEncoder

class OfferLinkComposer(private val endpoint: OfferEndpoint) {

    fun compose(advertisingId: String?, clickId: String?, pushToken: String?): String {
        val params = buildList {
            add(SOURCE to endpoint.source)
            add(ADVERTISING_ID to advertisingId.orEmpty())
            clickId?.takeIf { it.isNotBlank() }?.let { add(CLICK_ID to it) }
            add(PUSH_TOKEN to pushToken.orEmpty())
        }
        return params.joinToString(
            separator = "&",
            prefix = "${endpoint.baseUrl}?"
        ) { (key, value) ->
            "$key=${URLEncoder.encode(value, Charsets.UTF_8.name())}"
        }
    }

    companion object {
        const val SOURCE = "track_sr"
        const val ADVERTISING_ID = "track_gi"
        const val CLICK_ID = "track_cl"
        const val PUSH_TOKEN = "track_ph"
    }
}
