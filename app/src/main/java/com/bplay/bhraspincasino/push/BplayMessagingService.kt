package com.bplay.bhraspincasino.push

import com.bplay.bhraspincasino.data.PushTokenStore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class BplayMessagingService : FirebaseMessagingService() {

    private val tokenStore: PushTokenStore by inject()
    private val notifier: PushNotifier by inject()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        scope.launch { tokenStore.write(token) }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val data = message.data
        notifier.show(
            title = message.notification?.title ?: data[KEY_TITLE],
            body = message.notification?.body ?: data[KEY_BODY],
            data = data,
        )
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private companion object {
        const val KEY_TITLE = "title"
        const val KEY_BODY = "body"
    }
}
