package com.bplay.bhraspincasino.domain

interface PushTokenSource {
    suspend fun pushToken(): String?
}
