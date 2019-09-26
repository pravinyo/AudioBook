package com.allsoftdroid.feature_book.presentation.utility

import android.graphics.drawable.LayerDrawable
import android.content.Context
import android.graphics.drawable.Drawable



class CreateImageOverlay(private val context: Context) {

    companion object{
        fun with(context:Context)= CreateImageOverlay(context)
    }

    fun buildOverlay(front:Int,back:Int): Drawable {

        val layers = arrayOfNulls<Drawable>(2)
        layers[0] = context.getDrawable(back)
        layers[1] = context.getDrawable(front)

        return LayerDrawable(layers)
    }
}