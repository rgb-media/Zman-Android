package com.rgbmedia.zman.network

import com.rgbmedia.zman.models.MenuElement
import retrofit2.http.GET

interface MenuService {

    @GET("menu.json")
    suspend fun getMenuElements() : List<MenuElement>
}