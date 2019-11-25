package com.allsoftdroid.feature.book_details.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.allsoftdroid.feature.book_details.domain.repository.BookDetailsSharedPreferenceRepository
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject

class BookDetailsSharedPreferencesRepositoryImpl(private val preferences : SharedPreferences) : BookDetailsSharedPreferenceRepository {

    companion object {
        @JvmStatic
        fun create(context: Context): BookDetailsSharedPreferencesRepositoryImpl {
            val preferences = context.getSharedPreferences("BookDetailsRxPrefs", Context.MODE_PRIVATE)
            return BookDetailsSharedPreferencesRepositoryImpl(preferences)
        }

        private const val KEY_NAME_TRACK_PLAYING_NUMBER = "key_name_current_track_playing_number"
        private const val KEY_NAME_TRACK_PLAYING_TITLE = "key_name_current_track_playing_title"
        private const val KEY_NAME_TRACK_IS_PLAYING = "Key_name_is_track_playing"
    }

    override fun saveTrackPosition(pos: Int)=
        preferences.editSharedPreferences {
            putInt(KEY_NAME_TRACK_PLAYING_NUMBER,pos)
            }

    override fun trackPosition():Int = preferences.getInt(KEY_NAME_TRACK_PLAYING_NUMBER,0)

    fun saveIsPlaying(isPlaying: Boolean)=
        preferences.editSharedPreferences {
            putBoolean(KEY_NAME_TRACK_IS_PLAYING,isPlaying)
        }

    fun isPlaying():Boolean = preferences.getBoolean(KEY_NAME_TRACK_IS_PLAYING,false)

    override fun saveTrackTitle(title: String) =
        preferences.editSharedPreferences {
            putString(KEY_NAME_TRACK_PLAYING_TITLE,title)
            }

    override fun trackTitle():String = preferences.getString(KEY_NAME_TRACK_PLAYING_TITLE,"")?:""

    override fun clear() = preferences.clearSharedPreferences {
        remove(KEY_NAME_TRACK_PLAYING_TITLE)
        remove(KEY_NAME_TRACK_PLAYING_NUMBER)
        remove(KEY_NAME_TRACK_IS_PLAYING)
    }

    private fun SharedPreferences.editSharedPreferences(batch: SharedPreferences.Editor.() -> Unit) = edit().also(batch).apply()

    private fun SharedPreferences.clearSharedPreferences(batch: SharedPreferences.Editor.() -> Unit)= edit().also(batch).apply()
}