package com.allsoftdroid.feature_book.presentation

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.allsoftdroid.feature_book.R
import com.allsoftdroid.feature_book.domain.model.AudioBookDomainModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions

/**
Handle visibility of progress bar
 */
@BindingAdapter("goneIfNotNull")
fun goneIfNotNull(view: View, it: List<Any>?){
    view.visibility = if(it!=null && it.size>1) View.GONE else View.VISIBLE
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
            .load(url)
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .dontAnimate()
            .apply(
                RequestOptions()
                    .placeholder(R.drawable.loading_animation)
                    .error(R.drawable.notfound))
            .into(imageView)
    }
}

/**
 * Update list items desc details
 */
@BindingAdapter("bookDescription")
fun TextView.setBookDescription(item: AudioBookDomainModel?){
    item?.let {
        text = getNormalizedText("- by ${it.creator},  ${it.date}",70)
    }
}

/*
Binding adapter for updating the title in list items
 */
@BindingAdapter("bookTitle")
fun TextView.setBookTitle(item: AudioBookDomainModel?){
    item?.let {
        text = getNormalizedText(item.title,30)
    }
}

private fun getThumbnail(imageId: String?) = "https://archive.org/services/img/$imageId/"

private fun getNormalizedText(text:String?,limit:Int):String{
    if(text?.length?:0>limit){
        return text?.substring(0,limit-3)+"..."
    }

    return text?:""
}
