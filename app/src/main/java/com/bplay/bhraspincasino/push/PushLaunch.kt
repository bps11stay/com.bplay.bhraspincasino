package com.bplay.bhraspincasino.push

import android.content.Intent

data class PushLaunch(val clickId: String?, val offerUrl: String?) {

    companion object {
        const val KEY_CLICK_ID = "track_cl"
        const val KEY_OFFER_URL = "url"

        private val WEB_SCHEMES = listOf("https://", "http://")

        fun from(read: (String) -> String?): PushLaunch? {
            val clickId = read(KEY_CLICK_ID)?.trim()?.ifEmpty { null }
            val offerUrl = read(KEY_OFFER_URL)?.trim()
                ?.takeIf { url -> WEB_SCHEMES.any { url.startsWith(it, ignoreCase = true) } }
            if (clickId == null && offerUrl == null) return null
            return PushLaunch(clickId, offerUrl)
        }

        fun from(intent: Intent?): PushLaunch? {
            val extras = intent?.extras ?: return null
            return from { key -> extras.getString(key) }
        }
    }
}
