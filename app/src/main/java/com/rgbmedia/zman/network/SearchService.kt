package com.rgbmedia.zman.network

import com.rgbmedia.zman.models.SearchModel
import retrofit2.http.GET
import retrofit2.http.Query

interface SearchService {

    @GET("header_search.php")
    suspend fun search(@Query("action") action: String, @Query("find") text: String) : SearchModel
}