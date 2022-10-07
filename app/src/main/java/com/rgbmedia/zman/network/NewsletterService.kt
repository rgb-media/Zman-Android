package com.rgbmedia.zman.network

import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface NewsletterService {

    @FormUrlEncoded
    @POST("contacts.php")
    suspend fun sendEmail(@Field("email") email: String) : String
}