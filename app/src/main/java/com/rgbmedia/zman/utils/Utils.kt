package com.rgbmedia.zman.utils

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.appcompat.app.AlertDialog
import com.rgbmedia.zman.MENU_ANIMATION_DURATION

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
            val builder = AlertDialog.Builder(context)

            with(builder) {
                setTitle(title)
                setMessage(message)
                setPositiveButton("OK", null)
                show()
            }
        }
    }
}