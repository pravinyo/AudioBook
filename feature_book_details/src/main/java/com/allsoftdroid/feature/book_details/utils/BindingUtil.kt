package com.allsoftdroid.feature.book_details.utils

import android.graphics.Bitmap
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.BindingAdapter
import androidx.palette.graphics.Palette
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.model.BookDetails
import com.allsoftdroid.common.base.extension.CreateImageOverlay
import com.allsoftdroid.common.base.utils.BindingUtils.getNormalizedText
import com.allsoftdroid.common.base.utils.BindingUtils.getSignatureForImageLoading
import com.allsoftdroid.common.base.utils.BindingUtils.getThumbnail
import com.allsoftdroid.feature.book_details.R
import com.allsoftdroid.feature.book_details.domain.model.AudioBookMetadataDomainModel
import com.allsoftdroid.feature.book_details.domain.model.AudioBookTrackDomainModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.signature.ObjectKey
import kotlin.time.ExperimentalTime
import kotlin.time.seconds


@BindingAdapter("trackTitle")
fun TextView.setTrackTitle(item : AudioBookTrackDomainModel?){
    item?.let {
        item.title?.let { title->
            text = getNormalizedText(
                limit = 38,
                text = title)
        }
    }
}

@ExperimentalTime
@BindingAdapter("trackLength")
fun TextView.setTrackLength(item : AudioBookTrackDomainModel?){
    item?.let {
        item.length?.let {length->
            text = if(!length.contains(":")){
                val timeInSec = length.toFloat().toInt().seconds
                timeInSec.toComponents { minutes, seconds, _ ->
                    var sec = seconds.toString()
                    var min = minutes.toString()

                    if(seconds.toString().length==1){
                        sec = "0$seconds"
                    }

                    if (minutes.toString().length==1){
                        min = "0$minutes"
                    }

                    "$min:$sec"
                }
            } else length
        }
    }
}

@BindingAdapter("trackPlayingStatus")
fun setTrackPlayingStatus(imageView: ImageView,item :AudioBookTrackDomainModel?){
    item?.let {
        if(item.isPlaying){
            imageView.setImageResource(R.drawable.play_circle_green)
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
@BindingAdapter("bookBanner")
fun setBookBanner(layout: ConstraintLayout, item: AudioBookMetadataDomainModel?) {

    item?.let {
        val url =
            getThumbnail(item.identifier)

        val imageView = layout.findViewById<ImageView>(R.id.imgView_book_image)

        Glide
            .with(imageView.context)
            .asBitmap()
            .load(url)
            .dontAnimate()
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
            .listener(object : RequestListener<Bitmap>{
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
                                val dark = it.getDarkMutedColor(it.getDarkVibrantColor(0))
                                val dominant = it.getDominantColor(it.getVibrantColor(0))
                                val light = it.getLightMutedColor(it.getLightVibrantColor(0))

                                layout.setBackgroundColor(dominant)

                                with(layout.rootView.findViewById<View>(R.id.toolbar)){
                                    setBackgroundColor(dark)

                                    if(dark!=light){
                                        this.findViewById<TextView>(R.id.tv_toolbar_title).apply {
                                            this.setTextColor(light)
                                        }
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

/*
Binding adapter for updating the title in list items
 */
@BindingAdapter("bookTitle")
fun TextView.setBookTitle(item: AudioBookMetadataDomainModel?){
    item?.let {
        text = getNormalizedText(
            item.title,
            30
        )
    }
}

/*
Binding adapter for updating the title in list items
 */
@BindingAdapter("bookPlayTime1")
fun TextView.setBookPlayTime1(item: AudioBookMetadataDomainModel?){
    item?.let {
        if(text.isEmpty()){
            text = it.runtime
        }
    }
}

/*
Binding adapter for updating the title in list items
 */
@BindingAdapter("bookPlayTime2")
fun TextView.setBookPlayTime2(item: BookDetails?){
    item?.let {
        if(text=="NA"){
            if(it.runtime.isNotEmpty()){
                text = it.runtime
            }
        }
    }
}

@BindingAdapter("bookChapters")
fun TextView.setBookChapterCount(items: List<AudioBookTrackDomainModel>?){
    items?.let {
        text = if(it.isNotEmpty()) "${it.size} Chapters" else "NA"
    }
}

@BindingAdapter("bookTags")
fun TextView.setBookTags(item: AudioBookMetadataDomainModel?){
    item?.let {
        text = it.tag
    }
}

