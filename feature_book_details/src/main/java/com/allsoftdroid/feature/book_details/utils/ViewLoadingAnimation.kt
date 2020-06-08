package com.allsoftdroid.feature.book_details.utils

import android.animation.ObjectAnimator
import android.graphics.Color
import android.view.View

class ViewLoadingAnimation(private val view: View) {

    private lateinit var animator:ObjectAnimator

    fun colorize(){
        animator = ObjectAnimator.ofArgb(view,"backgroundColor",Color.GRAY,Color.LTGRAY)
        animator.duration = 500
        animator.repeatCount = ObjectAnimator.INFINITE
        animator.repeatMode = ObjectAnimator.REVERSE
        animator.start()
    }

    fun stop(){
        animator.cancel()
        view.setBackgroundColor(Color.TRANSPARENT)
    }
}