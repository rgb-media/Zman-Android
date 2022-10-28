package com.rgbmedia.zman.network

import com.rgbmedia.zman.models.LoginModel
import retrofit2.http.*

interface LoginService {

    @POST("app_user_login_actions.php")
    suspend fun loginWithFb(@Query("action") action: String,
                            @Query("user_id") user_id: String,
                            @Query("user_name") user_name: String,
                            @Query("user_email") user_email: String,
                            @Query("user_picture") user_picture: String,
                            @Query("access_token") access_token: String) : LoginModel
}