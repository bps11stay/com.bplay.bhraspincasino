package com.bplay.bhraspincasino.ui.gate

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.net.toUri
import java.net.URISyntaxException

class GateWebClient(
    private val onPageReady: () -> Unit,
    private val onPageUnavailable: () -> Unit,
) : WebViewClient() {

    private var mainFrameFailed = false

    override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
        mainFrameFailed = false
    }

    override fun onPageFinished(view: WebView, url: String?) {
        if (!mainFrameFailed) onPageReady()
    }

    override fun onReceivedError(
        view: WebView,
        request: WebResourceRequest,
        error: WebResourceError
    ) {
        if (!request.isForMainFrame) return
        mainFrameFailed = true
        onPageUnavailable()
    }

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        val target = request.url
        return when (target.scheme?.lowercase()) {
            "http", "https" -> false
            "intent" -> {
                openIntentScheme(view, target)
                true
            }

            null, "file", "content", "javascript", "data", "about" -> true
            else -> {
                startOutside(view.context, Intent(Intent.ACTION_VIEW, target))
                true
            }
        }
    }

    private fun openIntentScheme(view: WebView, target: Uri) {
        val intent = try {
            Intent.parseUri(target.toString(), Intent.URI_INTENT_SCHEME)
        } catch (_: URISyntaxException) {
            return
        }.apply {
            addCategory(Intent.CATEGORY_BROWSABLE)
            component = null
            selector = null
        }
        if (startOutside(view.context, intent)) return
        val fallback = intent.getStringExtra("browser_fallback_url")?.toUri()
        val fallbackScheme = fallback?.scheme?.lowercase()
        if (fallbackScheme == "http" || fallbackScheme == "https") {
            view.loadUrl(fallback.toString())
            return
        }
        intent.`package`?.let { store ->
            startOutside(
                view.context,
                Intent(Intent.ACTION_VIEW, "market://details?id=$store".toUri())
            )
        }
    }

    private fun startOutside(context: Context, intent: Intent): Boolean = try {
        context.startActivity(intent)
        true
    } catch (_: ActivityNotFoundException) {
        false
    } catch (_: SecurityException) {
        false
    }
}
