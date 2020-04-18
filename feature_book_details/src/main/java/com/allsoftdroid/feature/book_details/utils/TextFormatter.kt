package com.allsoftdroid.feature.book_details.utils

import android.graphics.Color
import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.data.model.BookDetails
import com.allsoftdroid.feature.book_details.domain.model.AudioBookMetadataDomainModel

class TextFormatter {
    companion object{
        fun formattedBookDetails(bookDetails: AudioBookMetadataDomainModel):CharSequence{
            val labelAuthor ="Author:\t"
            val labelRuntime ="\n\nRuntime:\t"
            val labelDescription = "\n\nDescription:\t"

            return Truss()
                .label(labelAuthor, Color.BLUE)
                .append(bookDetails.creator)

                .label(labelRuntime, Color.BLUE)
                .append(bookDetails.runtime)

                .label(labelDescription, Color.BLUE)
                .append(convertHtmlToText(bookDetails.description))
                .build()
        }

        fun formattedBookDetails(bookDetails: BookDetails):CharSequence{
            val labelAuthor ="Author:\t"
            val labelExtra ="\n\nExtra:\t"
            val labelLanguage="\n\nLanguage:\t"
            val labelRuntime ="\n\nRuntime:\t"
            val labelGenres = "\n\nGenre(s):\t"
            val labelReadText = "\n\nRead Text:\t"
            val labelDescription = "\n\nDescription:\t"

            return Truss()
                .label(labelAuthor, Color.BLUE)
                .append(bookDetails.webDocument?.author?:"NA")

                .label(labelExtra, Color.BLUE)
                .append(bookDetails.webDocument?.list.toString())

                .label(labelLanguage, Color.BLUE)
                .append(bookDetails.language)

                .label(labelRuntime, Color.BLUE)
                .append(bookDetails.runtime)

                .label(labelGenres, Color.BLUE)
                .append(bookDetails.genres)

                .label(labelReadText, Color.BLUE)
                .append(bookDetails.gutenbergUrl)

                .label(labelDescription, Color.BLUE)
                .append(bookDetails.description)
                .build()
        }
    }
}