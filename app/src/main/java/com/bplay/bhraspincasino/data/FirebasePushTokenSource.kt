package com.bplay.bhraspincasino.data

import com.bplay.bhraspincasino.domain.PushTokenSource
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

class FirebasePushTokenSource(
    private val store: PushTokenStore,
    private val backgroundScope: CoroutineScope,
    private val firstFetchTimeoutMillis: Long = FIRST_FETCH_TIMEOUT_MILLIS,
) : PushTokenSource {

    override suspend fun pushToken(): String? {
        val stored = store.read()
        if (stored != null) {
            backgroundScope.launch {
                fetchFromFirebase()?.takeIf { it != stored }?.let { store.write(it) }
            }
            return stored
        }
        val fetched = withTimeoutOrNull(firstFetchTimeoutMillis) { fetchFromFirebase() }
        fetched?.let { store.write(it) }
        return fetched
    }

    @Suppress("DEPRECATION")
    private suspend fun fetchFromFirebase(): String? =
        runCatching { FirebaseMessaging.getInstance().token.await() }
            .getOrNull()
            ?.ifBlank { null }

    private companion object {
        const val FIRST_FETCH_TIMEOUT_MILLIS = 10_000L
    }
}
