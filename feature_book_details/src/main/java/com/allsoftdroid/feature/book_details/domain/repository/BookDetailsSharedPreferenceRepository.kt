package com.allsoftdroid.feature.book_details.domain.repository

/**
 * Interface for sharePreference for saving current state of the track Playing
 */

interface BookDetailsSharedPreferenceRepository {

    fun saveTrackPosition(pos : Int)
    fun trackPosition():Int

    fun saveTrackTitle(title : String)
    fun trackTitle():String

    fun clear()
}