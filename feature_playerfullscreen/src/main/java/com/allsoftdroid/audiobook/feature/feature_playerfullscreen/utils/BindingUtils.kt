package com.allsoftdroid.audiobook.feature.feature_playerfullscreen.utils

import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.allsoftdroid.audiobook.feature.feature_playerfullscreen.R
import com.allsoftdroid.audiobook.feature.feature_playerfullscreen.data.PlayingTrackDetails
import com.allsoftdroid.common.base.extension.CreateImageOverlay
import com.allsoftdroid.common.base.network.ArchiveUtils.Companion.getThumbnail
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions


@BindingAdapter("trackTitle")
fun TextView.setTrackTitle(item : PlayingTrackDetails?){
    item?.let {
        text = item.trackName
    }?:"Unknown"
}

@BindingAdapter("trackBookTitle")
fun TextView.setTrackBookTitle(item : PlayingTrackDetails?){
    item?.let {
        text = item.bookTitle
    }?:"Unknown"
}

@BindingAdapter("bookChapterProgressTitle")
fun TextView.setBookChapterProgressTitle(item : PlayingTrackDetails?){
    item?.let {
        text = "Chapter ${item.chapterIndex} of ${item.totalChapter}"
    }?:"Unknown"
}


/**
load images using glide library
If content is not yet available to be displayed show loading animation
If content is not there show broken image
 */
@BindingAdapter("trackBookImage")
fun setTrackBookImageUrl(imageView: ImageView, item: PlayingTrackDetails?) {

    item?.let {
        val url =
            getThumbnail(item.bookIdentifier)

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