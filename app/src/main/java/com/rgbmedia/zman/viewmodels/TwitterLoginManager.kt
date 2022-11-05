package com.rgbmedia.zman.viewmodels

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.twitter.sdk.android.core.*
import com.twitter.sdk.android.core.identity.TwitterAuthClient
import com.twitter.sdk.android.core.models.User
import com.rgbmedia.zman.R

class TwitterLoginManager(private val activity: Activity, private val mainViewModel: MainViewModel) {

    private val TAG = "TwitterLogin"

    private val twitterAuthClient: TwitterAuthClient

    init {
        val authConfig = TwitterAuthConfig(activity.getString(R.string.twitter_consumer_key), activity.getString(R.string.twitter_consumer_secret))

        val config = TwitterConfig.Builder(activity)
            .twitterAuthConfig(authConfig)
            .debug(true)
            .build()
        Twitter.initialize(config)

        twitterAuthClient = TwitterAuthClient()
    }

    fun login() {
        twitterAuthClient.authorize(activity, object : Callback<TwitterSession>() {
            override fun success(result: Result<TwitterSession>?) {
                Log.d(TAG, "authorize.success")

                TwitterCore.getInstance().apiClient.accountService.verifyCredentials(false, false, true).enqueue(object : Callback<User>() {
                    override fun success(result: Result<User>?) {
                        Log.d(TAG, "verifyCredentials.success: ${result?.data.toString()}")

                        val userEmail = result!!.data.email
                        val screenName = result.data.screenName

                        mainViewModel.loginWithTwitter(userEmail, screenName)
                    }

                    override fun failure(exception: TwitterException) {
                        Log.d(TAG, "verifyCredentials.failure: $exception")

                        mainViewModel.setLoginError(exception.localizedMessage)
                    }
                })
            }

            override fun failure(e: TwitterException) {
                Log.d(TAG, "authorize.failure: $e")

                mainViewModel.setLoginError(e.localizedMessage)
            }
        })
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        twitterAuthClient.onActivityResult(requestCode, resultCode, data)
    }
}