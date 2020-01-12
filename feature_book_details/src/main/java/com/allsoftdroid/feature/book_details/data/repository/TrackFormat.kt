package com.allsoftdroid.feature.book_details.data.repository

sealed class TrackFormat{
    object FormatBP64 : TrackFormat()
    object FormatBP128 : TrackFormat()
    object FormatVBR : TrackFormat()
}
