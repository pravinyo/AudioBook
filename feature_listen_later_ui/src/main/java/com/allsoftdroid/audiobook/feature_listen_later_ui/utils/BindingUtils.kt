package com.allsoftdroid.audiobook.feature_listen_later_ui.utils

import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.allsoftdroid.audiobook.feature_listen_later_ui.R
import com.allsoftdroid.audiobook.feature_listen_later_ui.data.model.ListenLaterItemDomainModel
import com.allsoftdroid.common.base.extension.CreateImageOverlay
import com.allsoftdroid.common.base.network.ArchiveUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions


@BindingAdapter("bookImage")
fun setImageUrl(imageView: ImageView, item: ListenLaterItemDomainModel?) {

    item?.let {
        val url = ArchiveUtils.getThumbnail(item.identifier)

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


/*
Binding adapter for updating the title in list items
 */
@BindingAdapter("bookTitle")
fun TextView.setBookTitle(item: ListenLaterItemDomainModel?){
    item?.let {
        text =
            getNormalizedText(item.title, 30)
    }
}

@BindingAdapter("bookAuthor")
fun TextView.setBookAuthor(item: ListenLaterItemDomainModel?){
    item?.let {
        text =
            getNormalizedText(item.author, 30)
    }
}

@BindingAdapter("bookDuration")
fun TextView.setBookDuration(item: ListenLaterItemDomainModel?){
    item?.let {
        text = it.duration
    }
}

private fun getNormalizedText(text:String?,limit:Int):String{
    if(text?.length?:0>limit){
        return text?.substring(0,limit-3)+"..."
    }

    return text?:""
}