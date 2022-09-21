package com.rgbmedia.zman.utils

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.view.View
import android.view.animation.LinearInterpolator
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
    }
}