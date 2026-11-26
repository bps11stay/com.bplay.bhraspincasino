package com.bplay.bhraspincasino.ui.gate

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView

class GateChromeClient(private val openPicker: (Intent) -> Boolean) : WebChromeClient() {

    private var awaitingFiles: ValueCallback<Array<Uri>>? = null

    override fun onShowFileChooser(
        webView: WebView,
        filePathCallback: ValueCallback<Array<Uri>>,
        fileChooserParams: FileChooserParams,
    ): Boolean {
        dropPending()
        val picker = fileChooserParams.createIntent().apply {
            if (fileChooserParams.mode == FileChooserParams.MODE_OPEN_MULTIPLE) {
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            }
        }
        if (!openPicker(picker)) return false
        awaitingFiles = filePathCallback
        return true
    }

    fun onPickerResult(resultCode: Int, data: Intent?) {
        val callback = awaitingFiles ?: return
        awaitingFiles = null
        val picked = if (resultCode != Activity.RESULT_OK) {
            null
        } else {
            data?.clipData
                ?.let { clip -> List(clip.itemCount) { clip.getItemAt(it).uri }.filterNotNull() }
                ?.takeIf { it.isNotEmpty() }
                ?.toTypedArray()
                ?: FileChooserParams.parseResult(resultCode, data)
        }
        callback.onReceiveValue(picked)
    }

    fun dropPending() {
        awaitingFiles?.onReceiveValue(null)
        awaitingFiles = null
    }
}
