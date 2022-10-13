package com.rgbmedia.zman.network

import com.rgbmedia.zman.models.SearchModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SearchRepository(private val api: SearchService) {

    suspend fun search(text: String): SearchModel {
        return withContext(Dispatchers.IO) {
            api.search("find", text)
        }
    }
}