package com.rgbmedia.zman.network

import com.rgbmedia.zman.MENU_URL
import com.rgbmedia.zman.models.MenuElement
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface MenuApi {

    @GET("menu.json")
    fun getMovies() : Call<List<MenuElement>>

    companion object {

        fun create() : MenuApi {
            val retrofit = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(MENU_URL)
                .build()

            return retrofit.create(MenuApi::class.java)
        }
    }
}