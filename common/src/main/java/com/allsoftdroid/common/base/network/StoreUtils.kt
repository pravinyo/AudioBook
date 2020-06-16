package com.allsoftdroid.common.base.network

import android.content.Context

object StoreUtils {
    fun getStoreUrl(context:Context) : String{
        return "https://play.google.com/store/apps/details?id=${context.packageName}"
    }
}