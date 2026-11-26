package com.bplay.bhraspincasino.data

import com.bplay.bhraspincasino.domain.PushTokenSource
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

class FirebasePushTokenSource(
    private val store: PushTokenStore,
    private val firstFetchTimeoutMillis: Long = FIRST_FETCH_TIMEOUT_MILLIS,
) : PushTokenSource {

    override suspend fun pushToken(): String? {
        store.read()?.let { return it }
        val fetched = withTimeoutOrNull(firstFetchTimeoutMillis) {
            runCatching { FirebaseMessaging.getInstance().token.await() }.getOrNull()
        }?.ifBlank { null }
        fetched?.let { store.write(it) }
        return fetched
    }

    private companion object {
        const val FIRST_FETCH_TIMEOUT_MILLIS = 10_000L
    }
}
