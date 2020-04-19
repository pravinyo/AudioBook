package com.allsoftdroid.audiobook.services.audio.utils

import android.os.Build
import android.text.Html

class TextFormatter {
    companion object{
        fun getPartialString(text:String):String{
            val filterText = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(text,Html.FROM_HTML_MODE_COMPACT).toString()
            } else {
                Html.fromHtml(text).toString()
            }
            return if(filterText.length>23){
                filterText.substring(0,20)+"..."
            }else filterText
        }
    }
}