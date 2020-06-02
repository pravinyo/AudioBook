package com.allsoftdroid.audiobook.feature.feature_playerfullscreen.utils

import android.graphics.Bitmap
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.databinding.BindingAdapter
import androidx.palette.graphics.Palette
import com.allsoftdroid.audiobook.feature.feature_playerfullscreen.R
import com.allsoftdroid.audiobook.feature.feature_playerfullscreen.data.PlayingTrackDetails
import com.allsoftdroid.common.base.extension.CreateImageOverlay
import com.allsoftdroid.common.base.utils.BindingUtils.getNormalizedText
import com.allsoftdroid.common.base.utils.BindingUtils.getSignatureForImageLoading
import com.allsoftdroid.common.base.utils.BindingUtils.getThumbnail
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.signature.ObjectKey


@BindingAdapter("trackTitle")
fun TextView.setTrackTitle(item : PlayingTrackDetails?){
    item?.let {
        text = getNormalizedText(item.trackName,94)
    }
}

@BindingAdapter("trackBookTitle")
fun TextView.setTrackBookTitle(item : PlayingTrackDetails?){
    item?.let {
        text = getNormalizedText(item.bookTitle,30)
    }
}

@BindingAdapter("bookChapterProgressTitle")
fun TextView.setBookChapterProgressTitle(item : PlayingTrackDetails?){
    item?.let {
        text = context.getString(R.string.book_chapter_progress_title,item.chapterIndex,item.totalChapter)
    }
}


/**
load images using glide library
If content is not yet available to be displayed show loading animation
If content is not there show broken image
 */
@BindingAdapter("trackBookBanner")
fun setTrackBookBanner(cardView: CardView, item: PlayingTrackDetails?) {

    item?.let {
        val url =
            getThumbnail(item.bookIdentifier)

        val imageView = cardView.findViewById<ImageView>(R.id.book_thumbnail)

        Glide
            .with(imageView.context)
            .asBitmap()
            .load(url)
            .apply(
                RequestOptions()
                    .signature(ObjectKey(getSignatureForImageLoading(item.bookIdentifier)))
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .placeholder(R.drawable.loading_animation)
                    .error(
                        CreateImageOverlay
                            .with(imageView.context)
                            .buildOverlay(front = R.drawable.ic_book_play,back = R.drawable.gradiant_background)
                    )
            )
            .dontAnimate()
            .listener(object : RequestListener<Bitmap> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }

                override fun onResourceReady(
                    resource: Bitmap?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {

                    resource?.let {
                        val paletteBuilder = Palette.from(resource)
                        paletteBuilder.maximumColorCount(4)

                        paletteBuilder.generate{
                            it?.let {
                                val dark = it.getDarkMutedColor(it.getMutedColor(0))
                                val dominant = it.getDominantColor(it.getVibrantColor(0))
                                val light = it.getLightMutedColor(it.getLightVibrantColor(0))

//                                cardView.setBackgroundColor(dominant)

                                with(cardView.rootView.findViewById<View>(R.id.parentContainer)){
                                    setBackgroundColor(dark)

                                    cardView.findViewById<TextView>(R.id.tv_book_title).apply {
                                        if(dark == light){
                                            this.setTextColor(context.resources.getColor(R.color.colorAccent))
                                        }else{
                                            this.setTextColor(light)
                                        }
                                    }

                                    this.findViewById<TextView>(R.id.tv_book_progress_title).apply {
                                        this.setTextColor(light)
                                    }
                                    this.findViewById<TextView>(R.id.tv_book_chapter_name).apply {
                                        this.setTextColor(light)
                                    }
                                    this.findViewById<TextView>(R.id.tv_book_progress_time).apply {
                                        this.setTextColor(light)
                                    }
                                }
                            }
                        }
                    }

                    return false
                }
            })
            .into(imageView)
    }
}