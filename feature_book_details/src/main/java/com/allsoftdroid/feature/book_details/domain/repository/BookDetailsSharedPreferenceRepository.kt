package com.allsoftdroid.feature.book_details.domain.repository

import io.reactivex.Completable
import io.reactivex.Observable

/**
 * Interface for sharePreference for saving current state of the track Playing
 */

interface BookDetailsSharedPreferenceRepository {

    fun saveTrackPosition(pos : Int) : Completable
    fun trackPosition() : Observable<Int>

    fun saveTrackTitle(title : String) : Completable
    fun trackTitle() : Observable<String>

    fun clear() : Completable
}