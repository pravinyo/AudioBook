package com.allsoftdroid.feature.book_details.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.allsoftdroid.feature.book_details.domain.repository.BookDetailsSharedPreferenceRepository
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject

class BookDetailsSharedPreferencesRepositoryImpl(preferences : SharedPreferences) : BookDetailsSharedPreferenceRepository {

    private val prefSubject = BehaviorSubject.createDefault(preferences)

    private val prefChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, _ ->
        prefSubject.onNext(sharedPreferences)
    }


    companion object {

        @JvmStatic
        fun create(context: Context): BookDetailsSharedPreferencesRepositoryImpl {
            val preferences = context.getSharedPreferences("BookDetailsRxPrefs", Context.MODE_PRIVATE)
            return BookDetailsSharedPreferencesRepositoryImpl(preferences)
        }

        private const val KEY_NAME_TRACK_PLAYING_NUMBER = "key_name_current_track_playing_number"
        private const val KEY_NAME_TRACK_PLAYING_TITLE = "key_name_current_track_playing_title"

    }

    init {
        preferences.registerOnSharedPreferenceChangeListener(prefChangeListener)
    }

    override fun saveTrackPosition(pos: Int): Completable = prefSubject
        .firstOrError()
        .editSharedPreferences {
            putInt(KEY_NAME_TRACK_PLAYING_NUMBER,pos)
        }

    override fun trackPosition(): Observable<Int> = prefSubject
        .map {
            it.getInt(KEY_NAME_TRACK_PLAYING_NUMBER,-1)
        }

    override fun saveTrackTitle(title: String): Completable = prefSubject
        .firstOrError()
        .editSharedPreferences {
            putString(KEY_NAME_TRACK_PLAYING_TITLE,title)
        }

    override fun trackTitle(): Observable<String> = prefSubject
        .map {
            it.getString(KEY_NAME_TRACK_PLAYING_TITLE,"")
        }

    override fun clear(): Completable = prefSubject
        .firstOrError()
        .clearSharedPreferences {
            remove(KEY_NAME_TRACK_PLAYING_TITLE)
            remove(KEY_NAME_TRACK_PLAYING_NUMBER)
        }

    private fun Single<SharedPreferences>.editSharedPreferences(batch: SharedPreferences.Editor.() -> Unit): Completable =
        flatMapCompletable {
            Completable.fromAction {
                it.edit().also(batch).apply()
            }
        }

    private fun Single<SharedPreferences>.clearSharedPreferences(batch: SharedPreferences.Editor.() -> Unit): Completable =
        flatMapCompletable {
            Completable.fromAction {
                it.edit().also(batch).apply()
            }
        }
}