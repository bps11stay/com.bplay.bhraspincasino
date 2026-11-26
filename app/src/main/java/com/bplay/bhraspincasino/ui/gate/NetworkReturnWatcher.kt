package com.bplay.bhraspincasino.ui.gate

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Handler
import android.os.Looper

class NetworkReturnWatcher(context: Context, private val onNetworkBack: () -> Unit) {

    private val connectivity = context.getSystemService(ConnectivityManager::class.java)
    private val mainThread = Handler(Looper.getMainLooper())
    private var callback: ConnectivityManager.NetworkCallback? = null

    fun watch() {
        if (callback != null) return
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                mainThread.post {
                    if (callback === this) {
                        stop()
                        onNetworkBack()
                    }
                }
            }
        }
        callback = networkCallback
        runCatching { connectivity.registerNetworkCallback(request, networkCallback) }
            .onFailure { callback = null }
    }

    fun stop() {
        val active = callback ?: return
        callback = null
        runCatching { connectivity.unregisterNetworkCallback(active) }
    }
}
