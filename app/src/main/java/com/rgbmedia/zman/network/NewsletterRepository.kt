package com.rgbmedia.zman.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NewsletterRepository(private val api: NewsletterService) {

    suspend fun sendEmail(email: String): String {
        return withContext(Dispatchers.IO) {
            api.sendEmail(email)
        }
    }
}