package com.rgbmedia.zman.network

import com.rgbmedia.zman.models.MenuElement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MenuRepository(private val api: MenuService) {

    suspend fun getMenu(): List<MenuElement> {
        return withContext(Dispatchers.IO) {
            api.getMenuElements()
        }
    }
}