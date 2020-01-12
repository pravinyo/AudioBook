package com.allsoftdroid.feature.book_details.presentation

import android.os.Build
import android.text.Html
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.allsoftdroid.common.base.extension.CreateImageOverlay
import com.allsoftdroid.feature.book_details.R
import com.allsoftdroid.feature.book_details.domain.model.AudioBookMetadataDomainModel
import com.allsoftdroid.feature.book_details.domain.model.AudioBookTrackDomainModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions



@BindingAdapter("trackTitle")
fun TextView.setTrackTitle(item : AudioBookTrackDomainModel?){
    item?.let {
        text = getNormalizedText(item.title,38)
    }
}

@BindingAdapter("trackLength")
fun TextView.setTrackLength(item : AudioBookTrackDomainModel?){
    item?.let {
        text = item.length
    }
}

@BindingAdapter("trackPlayingStatus")
fun setTrackPlayingStatus(imageView: ImageView,item :AudioBookTrackDomainModel?){
    item?.let {
        if(item.isPlaying){
            imageView.setImageResource(R.drawable.play_circle)
        }else{
            imageView.setImageResource(R.drawable.play_circle_outline)
        }
    }
}

/**
Handle visibility of progress bar
 */
@BindingAdapter("goneIfNotNull")
fun goneIfNotNull(view: View, it: Any?){
    view.visibility = if(it!=null) View.GONE else View.VISIBLE
}

/**
Handle visibility of inverse of progress bar
 */
@BindingAdapter("goneIfNull")
fun goneIfNull(view: View, it: Any?){
    view.visibility = if(it==null) View.GONE else View.VISIBLE
}

/**
load images using glide library
If content is not yet available to be displayed show loading animation
If content is not there show broken image
 */
@BindingAdapter("bookImage")
fun setImageUrl(imageView: ImageView, item: AudioBookMetadataDomainModel?) {

    item?.let {
        val url = getThumbnail(item.identifier)

        Glide
            .with(imageView.context)
            .asBitmap()
            .load(url)
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
fun TextView.setBookDescription(item: AudioBookMetadataDomainModel?){
    item?.let {
        text = convertHtmlToText(it.description)
    }
}

/*
Binding adapter for updating the title in list items
 */
@BindingAdapter("bookTitle")
fun TextView.setBookTitle(item: AudioBookMetadataDomainModel?){
    item?.let {
        text = getNormalizedText(item.title, 30)
    }
}

private fun getThumbnail(imageId: String?) = "https://archive.org/services/img/$imageId/"

private fun getNormalizedText(text:String?,limit:Int):String{
    if(text?.length?:0>limit){
        return text?.substring(0,limit-3)+"..."
    }

    return text?:""
}

private fun convertHtmlToText(text:String?) = text?.let {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(it, Html.FROM_HTML_MODE_COMPACT)
    } else {
        Html.fromHtml(it)
    }
} .toString()