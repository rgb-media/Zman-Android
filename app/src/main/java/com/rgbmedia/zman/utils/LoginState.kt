package com.rgbmedia.zman.utils

import android.content.Context
import com.rgbmedia.zman.ZmanApplication

enum class LoginStatus {
    notLoggedIn, loggedInWithFb, loggedInWithTwitter
}

class LoginState {
    companion object {
        val PREFERENCES_NAME = "ZmanLogin"

        val LOGIN_STATUS_KEY = "loginStatus"
        val LOGIN_DATA_KEY   = "loginData"

        fun getLoginStatus(): LoginStatus {
            val sharedPreferences = ZmanApplication.instance.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

            var statusString = sharedPreferences.getString(LOGIN_STATUS_KEY, "")
            if (statusString!!.isEmpty()) {
                statusString = LoginStatus.notLoggedIn.name
            }

            return LoginStatus.valueOf(statusString!!)
        }

        fun setLoginStatus(status: LoginStatus) {
            val sharedPreferences =  ZmanApplication.instance.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            var editor = sharedPreferences.edit()
            editor.putString(LOGIN_STATUS_KEY, status.name)
            editor.commit()
        }

        fun getLoginData(): String {
            val sharedPreferences = ZmanApplication.instance.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            val data = sharedPreferences.getString(LOGIN_DATA_KEY, "")

            return data!!
        }

        fun setLoginData(data: String) {
            val sharedPreferences =  ZmanApplication.instance.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            var editor = sharedPreferences.edit()
            editor.putString(LOGIN_DATA_KEY, data)
            editor.commit()
        }

        fun isLoggedIn(): Boolean {
            val status = getLoginStatus()

            return status == LoginStatus.loggedInWithFb || status == LoginStatus.loggedInWithTwitter
        }

        fun getId(): String {
            return Utils.getProfileIdFromDataString(getLoginData())
        }

        fun getImage(): String {
            return Utils.getProfileImageFromDataString(getLoginData())
        }
    }
}