package com.allsoftdroid.feature.book_details.utils

import android.graphics.Color
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.model.BookDetails
import com.allsoftdroid.feature.book_details.domain.model.AudioBookMetadataDomainModel

object TextFormatter {
    fun formattedBookDetails(bookDetails: AudioBookMetadataDomainModel):CharSequence{
        val labelAuthor ="\n\nAuthor:\t"
        val labelTitle ="\n\nTitle : \t"

        return Truss()
            .label(labelTitle, Color.BLUE)
            .append(bookDetails.title)

            .label(labelAuthor, Color.BLUE)
            .append(bookDetails.creator)
            .build()
    }

    fun formattedBookDetails(bookDetails: BookDetails):CharSequence{
        val labelAuthor ="\n\nAuthor:\t"
        val labelTitle ="\n\nTitle : \t"
        val labelExtra ="\n\nExtra:\t"
        val labelLanguage="\n\nLanguage:\t"

        return Truss()
            .label(labelTitle, Color.BLUE)
            .append(bookDetails.webDocument?.title?:"")

            .label(labelAuthor, Color.BLUE)
            .append(bookDetails.webDocument?.author?:"NA")

            .label(labelExtra, Color.BLUE)
            .append(bookDetails.webDocument?.list.toString())

            .label(labelLanguage, Color.BLUE)
            .append(bookDetails.language)
            .build()
    }
}