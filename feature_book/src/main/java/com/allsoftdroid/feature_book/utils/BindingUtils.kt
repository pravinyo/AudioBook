package com.allsoftdroid.feature_book.utils

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.allsoftdroid.common.base.extension.CreateImageOverlay
import com.allsoftdroid.common.base.utils.BindingUtils.convertDateToTime
import com.allsoftdroid.common.base.utils.BindingUtils.getNormalizedText
import com.allsoftdroid.common.base.utils.BindingUtils.getSignatureForImageLoading
import com.allsoftdroid.common.base.utils.BindingUtils.getThumbnail
import com.allsoftdroid.feature_book.R
import com.allsoftdroid.feature_book.domain.model.AudioBookDomainModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey

/**
Handle visibility of progress bar
 */
@BindingAdapter("searchOrClose")
fun searchOrClose(view: ImageView, isSearchBtn:Boolean){

   view.visibility =  when(view.id){
        R.id.iv_search -> {
            if(isSearchBtn) View.VISIBLE else View.GONE
        }

        R.id.iv_search_cancel -> {
            if(isSearchBtn) View.GONE else View.VISIBLE
        }

       else -> View.GONE
   }
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
            .apply(
                RequestOptions()
                    .signature(ObjectKey(getSignatureForImageLoading(item.date)))
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .placeholder(R.drawable.loading_animation)
                    .error(
                        CreateImageOverlay
                            .with(imageView.context)
                            .buildOverlay(front = R.drawable.ic_book_play,back = R.drawable.gradiant_background)
                    )
            )
            .dontAnimate()
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
                it.addeddate?:it.date,
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
        text =
            getNormalizedText(item.title, 30)
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