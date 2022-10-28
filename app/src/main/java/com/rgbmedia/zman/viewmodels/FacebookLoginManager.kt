package com.rgbmedia.zman.viewmodels

import android.app.Activity
import android.os.Bundle
import android.util.Log
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import org.json.JSONObject

class FacebookLoginManager(private val mainViewModel: MainViewModel) {

    private val TAG = "FacebookLogin"

    fun login(activity: Activity, callbackManager: CallbackManager) {
        val token = AccessToken.getCurrentAccessToken()
        if (token != null && !token.isExpired) {
            getProfile(token)
        } else {
            startLogin(activity, callbackManager)
        }
    }

    private fun startLogin(activity: Activity, callbackManager: CallbackManager) {
        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                val token = loginResult.accessToken

                if (token != null && !token.isExpired) {
                    getProfile(token)
                }

                Log.d(TAG, "onSuccess: ${token.token}")
            }

            override fun onCancel() {
                Log.d(TAG, "onCancel")

                mainViewModel.setLoginError("Facebook login was cancelled")
            }

            override fun onError(error: FacebookException) {
                Log.d(TAG, "onError")

                mainViewModel.setLoginError(error.localizedMessage)
            }
        })

        LoginManager.getInstance().logInWithReadPermissions(activity, listOf("public_profile", "email"))
    }

    private fun getProfile(accessToken: AccessToken) {
        val request = GraphRequest.newMeRequest(accessToken, object : GraphRequest.GraphJSONObjectCallback {
            override fun onCompleted(obj: JSONObject?, response: GraphResponse?) {
                val userId = obj?.get("id") as String
                val userName = obj?.get("name") as String
                val userEmail = obj?.get("email") as String
                val picture = obj?.get("picture") as JSONObject
                val data = picture.get("data") as JSONObject
                val userPicture = data.get("url") as String

                mainViewModel.loginWithFb(userId, userName, userEmail, userPicture, accessToken.token)

                Log.d(TAG, "GraphRequest.onCompleted")
            }
        })

        val parameters = Bundle()
        parameters.putString("fields", "id,name,email,picture.type(large)")
        request.parameters = parameters
        request.executeAsync()
    }
}