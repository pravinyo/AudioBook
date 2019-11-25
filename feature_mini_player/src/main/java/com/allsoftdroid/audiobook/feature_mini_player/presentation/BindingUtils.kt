package com.allsoftdroid.audiobook.feature_mini_player.presentation

import android.widget.Button
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import com.allsoftdroid.audiobook.feature_mini_player.R
import com.allsoftdroid.common.base.extension.CreateImageOverlay
import com.allsoftdroid.common.base.extension.Event
import com.allsoftdroid.common.base.network.ArchiveUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions

@BindingAdapter("controlIconPlayPause")
fun setPlayPauseIcon(view: Button, shouldPlay: Boolean){
    if(shouldPlay){
        view.setBackgroundResource(R.drawable.pause_circle)

    }else{
        view.setBackgroundResource(R.drawable.play_circle_outline)
    }
}

@BindingAdapter("bookImage")
fun setImageUrl(imageView: ImageView, bookId:String?) {

    bookId?.let {
        val url = ArchiveUtils.getThumbnail(bookId)

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

