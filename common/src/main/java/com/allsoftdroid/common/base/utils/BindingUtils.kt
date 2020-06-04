package com.allsoftdroid.common.base.utils

import android.content.Context
import android.os.Build
import com.allsoftdroid.common.base.network.ArchiveUtils
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

object BindingUtils {
    private val date by lazy {
        Date(System.currentTimeMillis())
    }

    fun getSignatureForImageLoading(published:String?=null):String{
        return published ?: getCurrentDate()
    }

    private fun getCurrentDate():String{
        val format = SimpleDateFormat("yyyy.MM.dd")
        return format.format(date)
    }

    fun getNormalizedText(text:String?,limit:Int):String{
        if(text?.length?:0>limit){
            return text?.substring(0,limit-3)+"..."
        }

        return text?:""
    }

    fun getThumbnail(imageId: String?) = ArchiveUtils.getThumbnail(imageId)

    fun convertDateToTime(date:String?,context: Context) = date?.let {
        calculateDateDiff(it, context)
    }?:"-"


    @Suppress("DEPRECATION", "RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    private fun calculateDateDiff(dateStr: String, context: Context?): String {

        if (context == null) return "-"

        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",
            getCurrentLocale(context)
        )
        format.timeZone = TimeZone.getTimeZone("UTC")

        try {

            val date = format.parse(dateStr)
            val diff = Date().time - date.time //this is going to give you the difference in milliseconds

            val result = Date(diff)

            return if (result.year - 70 > 0) {
                "${result.year - 70}y"
            } else if (result.month > 0) {
                "${1 + result.month}m"
            } else {
                "${result.date}d"
            }

        } catch (e: Exception) {
            Timber.e(e.printStackTrace().toString())
            return "-"
        }
    }

    @Suppress("DEPRECATION")
    private fun getCurrentLocale(context: Context): Locale {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.resources.configuration.locales.get(0)
        } else {
            context.resources.configuration.locale
        }
    }

}