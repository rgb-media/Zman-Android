package com.rgbmedia.zman.network

import android.util.Log
import com.rgbmedia.zman.models.MenuElement
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MenuRepository {

    companion object {
        fun getMenu(callback: (List<MenuElement>?) -> Unit) {
            val apiInterface = MenuApi.create().getMovies()

            apiInterface.enqueue(object : Callback<List<MenuElement>> {
                override fun onResponse(call: Call<List<MenuElement>>, response: Response<List<MenuElement>>) {
                    response.body()?.let {
                        Log.d("MenuRepository", "onResponse - Number of menu items: " + it.size)

                        callback(it)
                    }
                }

                override fun onFailure(call: Call<List<MenuElement>>, t: Throwable) {
                    Log.d("MenuRepository", "onFailure: $t")

                    callback(null)
                }
            })
        }
    }
}