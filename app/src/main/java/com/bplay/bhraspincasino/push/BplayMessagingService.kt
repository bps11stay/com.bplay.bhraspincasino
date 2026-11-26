package com.bplay.bhraspincasino.push

import com.bplay.bhraspincasino.data.PushTokenStore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject

class BplayMessagingService : FirebaseMessagingService() {

    private val tokenStore: PushTokenStore by inject()
    private val notifier: PushNotifier by inject()

    @Suppress("OVERRIDE_DEPRECATION")
    override fun onNewToken(token: String) {
        runBlocking { tokenStore.write(token) }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val data = message.data
        notifier.show(
            title = message.notification?.title ?: data[KEY_TITLE],
            body = message.notification?.body ?: data[KEY_BODY],
            data = data,
        )
    }

    private companion object {
        const val KEY_TITLE = "title"
        const val KEY_BODY = "body"
    }
}
