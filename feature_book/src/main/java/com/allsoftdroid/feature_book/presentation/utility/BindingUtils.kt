package com.allsoftdroid.feature_book.presentation.utility

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.allsoftdroid.common.base.extension.CreateImageOverlay
import com.allsoftdroid.common.base.network.ArchiveUtils.Companion.getThumbnail
import com.allsoftdroid.feature_book.R
import com.allsoftdroid.feature_book.domain.model.AudioBookDomainModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

/**
Handle visibility of progress bar
 */
@BindingAdapter("goneIfNotNull")
fun goneIfNotNull(view: View, it: List<Any>?){
    view.visibility = if(it!=null && it.size>1) View.GONE else View.VISIBLE
}

@BindingAdapter("goneIfNotSearchError")
fun goneIfNotSearchError(view: View, error: Boolean){
    view.visibility = if(!error) View.GONE else View.VISIBLE
}

/*
load images using glide library
If content is not yet available to be displayed show loading animation
If content is not there show broken image
 */
@BindingAdapter("bookImage")
fun setImageUrl(imageView: ImageView, item: AudioBookDomainModel?) {

    item?.let {
        val url = getThumbnail(item.mId)

        Glide
            .with(imageView.context)
            .asBitmap()
            .load(url)
            .override(250,250)
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .dontAnimate()
            .apply(
                RequestOptions()
                    .placeholder(R.drawable.loading_animation)
                    .error(
                        CreateImageOverlay
                            .with(imageView.context)
                            .buildOverlay(front = R.drawable.ic_book_play,back = R.drawable.gradiant_background)
                    )
            )
            .into(imageView)
    }
}

/**
 * Update list items desc details
 */
@BindingAdapter("bookDescription")
fun TextView.setBookDescription(item: AudioBookDomainModel?){
    item?.let {
        text = getNormalizedText(
            "- by ${formattedCreators(it.creator)},  ${convertDateToTime(
                it.date,
                this.context
            )}", 70
        )
    }
}

fun formattedCreators(creators: String?): String {

    return if(creators!=null){
        if(creators.contains("[")){
            //multiple creator
            creators.split(",")[0].substring(1)+",Multiple"
        }else creators

    }else "N/A"

}

/*
Binding adapter for updating the title in list items
 */
@BindingAdapter("bookTitle")
fun TextView.setBookTitle(item: AudioBookDomainModel?){
    item?.let {
        text = getNormalizedText(item.title, 30)
    }
}

private fun getNormalizedText(text:String?,limit:Int):String{
    if(text?.length?:0>limit){
        return text?.substring(0,limit-3)+"..."
    }

    return text?:""
}

private fun convertDateToTime(date:String?,context: Context) = date?.let {
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


@BindingAdapter("toolbarItemVisibility")
fun setToolbarItemVisibility(view: View, showDisplaySearch: Boolean){

    if(showDisplaySearch){
        if(view.id==R.id.toolbar_side_1){
            view.visibility = View.GONE
        }else if(view.id == R.id.toolbar_side_2){
            view.visibility = View.VISIBLE
        }
    }else{
        if(view.id==R.id.toolbar_side_2){
            view.visibility = View.GONE
        }else if(view.id == R.id.toolbar_side_1){
            view.visibility = View.VISIBLE
        }
    }
}