package com.rgbmedia.zman.utils

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.content.DialogInterface
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.appcompat.app.AlertDialog
import com.google.gson.Gson
import com.rgbmedia.zman.MENU_ANIMATION_DURATION
import com.rgbmedia.zman.models.LoginData
import com.rgbmedia.zman.models.LoginModel

class Utils {
    companion object {
        fun slideView(view: View, currentHeight: Int, newHeight: Int) {
            val slideAnimator = ValueAnimator.ofInt(currentHeight, newHeight).setDuration(MENU_ANIMATION_DURATION)
            slideAnimator.addUpdateListener {
                val value = it.animatedValue as Int

                view.getLayoutParams().height = value
                view.requestLayout()
            }

            val animationSet = AnimatorSet()
            animationSet.interpolator = LinearInterpolator()
            animationSet.play(slideAnimator)
            animationSet.start()
        }

        fun isValidEmail(email: String): Boolean {
            val regex = Regex("[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,64}")

            return email.matches(regex)
        }

        fun showSimpleAlert(context: Context, title: String, message: String) {
            showSimpleAlert(context, title, message, null)
        }

        fun showSimpleAlert(context: Context, title: String, message: String, listener: DialogInterface.OnClickListener?) {
            val builder = AlertDialog.Builder(context)

            with(builder) {
                setTitle(title)
                setMessage(message)
                setPositiveButton("OK", listener)
                show()
            }
        }

        fun getDataStringFromLoginModel(model: LoginModel): String {
            val gson = Gson()

            return gson.toJson(model.data)
        }

        fun getCookieFromLoginModel(model: LoginModel): String {
            val data = getDataStringFromLoginModel(model)

            return "writer_data=" + data + "; expires=Sat, 01-Jan-2030 00:00:00 GMT; path=/"
        }

        fun getProfileIdFromDataString(dataString: String): String {
            val gson = Gson()

            val data = gson.fromJson(dataString, LoginData::class.java)

            return data.id
        }

        fun getProfileImageFromDataString(dataString: String): String {
            val gson = Gson()

            val data = gson.fromJson(dataString, LoginData::class.java)

            return data.image
        }

        fun getProfileNameFromDataString(dataString: String): String {
            val gson = Gson()

            val data = gson.fromJson(dataString, LoginData::class.java)

            return data.name
        }
    }
}