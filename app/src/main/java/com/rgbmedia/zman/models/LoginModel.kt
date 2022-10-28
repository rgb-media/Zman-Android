package com.rgbmedia.zman.models

import java.io.Serializable

data class LoginModel(var status: String,
                      var exists: Boolean,
                      var data: LoginData): Serializable

data class LoginData(var id: String,
                     var name: String,
                     var image :String): Serializable
