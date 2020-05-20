package com.allsoftdroid.audiobook.feature_mybooks.utils

import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.allsoftdroid.audiobook.feature_mybooks.R
import com.allsoftdroid.audiobook.feature_mybooks.data.model.LocalBookDomainModel
import com.allsoftdroid.common.base.extension.CreateImageOverlay
import com.allsoftdroid.common.base.network.ArchiveUtils
import com.allsoftdroid.common.base.utils.BindingUtils.getNormalizedText
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions

@BindingAdapter("bookImage")
fun setImageUrl(imageView: ImageView, item: LocalBookDomainModel?) {

    item?.let {
        val url = ArchiveUtils.getThumbnail(item.bookIdentifier)

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
fun TextView.setBookTitle(item: LocalBookDomainModel?){
    item?.let {
        text = getNormalizedText(item.bookTitle, 30)
    }
}

@BindingAdapter("bookAuthor")
fun TextView.setBookAuthor(item: LocalBookDomainModel?){
    item?.let {
        text =
            getNormalizedText(item.bookAuthor, 30)
    }
}

@BindingAdapter("bookChapterCount")
fun TextView.setBookDuration(item: LocalBookDomainModel?){
    item?.let {
        text = context.getString(R.string.chapters_label,it.bookChaptersDownloaded,it.totalChapters)
    }
}