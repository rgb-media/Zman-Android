package com.rgbmedia.zman.network

import com.rgbmedia.zman.models.LoginModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LoginRepository(private val api: LoginService) {

    suspend fun loginWithFb(userId: String,
                            userName: String,
                            userEmail: String,
                            userPicture: String,
                            accessToken: String) : LoginModel {
        return withContext(Dispatchers.IO) {
            api.loginWithFb("facebook", userId, userName, userEmail, userPicture, accessToken)
        }
    }
}