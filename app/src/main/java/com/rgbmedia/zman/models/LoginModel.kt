package com.rgbmedia.zman.models

import com.rgbmedia.zman.utils.LoginStatus
import java.io.Serializable

data class LoginModel(var type: LoginStatus,
                      var status: String,
                      var exists: Boolean,
                      var data: LoginData): Serializable

data class LoginData(var id: String,
                     var name: String,
                     var image :String): Serializable
