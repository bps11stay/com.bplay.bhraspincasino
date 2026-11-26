package com.bplay.bhraspincasino.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.sessionPreferences: DataStore<Preferences> by preferencesDataStore(name = "bplay_session")

class PushTokenStore(context: Context) {

    private val preferences = context.applicationContext.sessionPreferences

    suspend fun read(): String? = preferences.data
        .catch { emit(emptyPreferences()) }
        .map { it[PUSH_TOKEN]?.ifBlank { null } }
        .first()

    suspend fun write(token: String) {
        try {
            preferences.edit { it[PUSH_TOKEN] = token }
        } catch (_: IOException) {
        }
    }

    private companion object {
        val PUSH_TOKEN = stringPreferencesKey("push_token")
    }
}
