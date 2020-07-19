package com.allsoftdroid.feature.book_details.data.repository

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.allsoftdroid.common.test.wrapEspressoIdlingResource
import com.allsoftdroid.feature.book_details.domain.repository.BookDetailsSharedPreferenceRepository

class BookDetailsSharedPreferencesRepositoryImpl(private val preferences : SharedPreferences) : BookDetailsSharedPreferenceRepository {

    companion object {
        @JvmStatic
        fun create(context: Application): BookDetailsSharedPreferencesRepositoryImpl {
            val preferences = context.getSharedPreferences("BookDetailsRxPrefs", Context.MODE_PRIVATE)
            return BookDetailsSharedPreferencesRepositoryImpl(preferences)
        }

        private const val KEY_NAME_TRACK_PLAYING_NUMBER = "key_name_current_track_playing_number"
        private const val KEY_NAME_TRACK_PLAYING_TITLE = "key_name_current_track_playing_title"
        private const val KEY_NAME_TRACK_IS_PLAYING = "Key_name_is_track_playing"
        private const val KEY_NAME_BOOK_ID="key_name_book_id"
        private const val KEY_NAME_TRACK_FORMAT = "key_name_track_format"
        private const val KEY_NAME_BOOK_NAME = "key_name_book_name"
        private const val KEY_NAME_IS_TOOL_TIP_SHOWN = "key_name_is_tool_tip_shown"
    }

    override fun saveTrackPosition(pos: Int)=
        wrapEspressoIdlingResource {
            preferences.editSharedPreferences {
                putInt(KEY_NAME_TRACK_PLAYING_NUMBER,pos)
            }
        }

    override fun trackPosition():Int = wrapEspressoIdlingResource {
        preferences.getInt(KEY_NAME_TRACK_PLAYING_NUMBER,1)
    }

    override fun saveIsPlaying(isPlaying: Boolean)=
        wrapEspressoIdlingResource {
            preferences.editSharedPreferences {
                putBoolean(KEY_NAME_TRACK_IS_PLAYING,isPlaying)
            }
        }

    override fun isPlaying():Boolean = wrapEspressoIdlingResource {
        preferences.getBoolean(KEY_NAME_TRACK_IS_PLAYING,false)
    }

    override fun isToolTipShown(): Boolean = wrapEspressoIdlingResource {
        preferences.getBoolean(KEY_NAME_IS_TOOL_TIP_SHOWN,false)
    }

    override fun setToolTipShown(shouldSkip: Boolean) = wrapEspressoIdlingResource {
        preferences.editSharedPreferences {
            putBoolean(KEY_NAME_IS_TOOL_TIP_SHOWN,shouldSkip)
        }
    }

    override fun saveTrackTitle(title: String) =
        wrapEspressoIdlingResource {
            preferences.editSharedPreferences {
                putString(KEY_NAME_TRACK_PLAYING_TITLE,title)
            }
        }

    override fun trackTitle():String = wrapEspressoIdlingResource {
        preferences.getString(KEY_NAME_TRACK_PLAYING_TITLE,"")?:""
    }

    override fun saveBookId(bookId: String) {
        wrapEspressoIdlingResource {
            preferences.editSharedPreferences {
                putString(KEY_NAME_BOOK_ID,bookId)
            }
        }
    }

    override fun bookId(): String = preferences.getString(KEY_NAME_BOOK_ID,"")?:""

    override fun saveBookName(name: String) {
        wrapEspressoIdlingResource {
            preferences.editSharedPreferences {
                putString(KEY_NAME_BOOK_NAME,name)
            }
        }
    }

    override fun bookName(): String = wrapEspressoIdlingResource {
        preferences.getString(KEY_NAME_BOOK_NAME,"N/A")?:"N/A"
    }

    override fun saveTrackFormatIndex(formatIndex: Int) {
        wrapEspressoIdlingResource {
            preferences.editSharedPreferences {
                putInt(KEY_NAME_TRACK_FORMAT,formatIndex)
            }
        }
    }

    override fun trackFormatIndex(): Int = wrapEspressoIdlingResource {
        preferences.getInt(KEY_NAME_TRACK_FORMAT,0)
    }

    override fun clear() = wrapEspressoIdlingResource {
        preferences.clearSharedPreferences {
            remove(KEY_NAME_TRACK_PLAYING_TITLE)
            remove(KEY_NAME_TRACK_PLAYING_NUMBER)
            remove(KEY_NAME_TRACK_IS_PLAYING)
            remove(KEY_NAME_BOOK_ID)
            remove(KEY_NAME_TRACK_FORMAT)
        }
    }

    private fun SharedPreferences.editSharedPreferences(batch: SharedPreferences.Editor.() -> Unit) = edit().also(batch).apply()

    private fun SharedPreferences.clearSharedPreferences(batch: SharedPreferences.Editor.() -> Unit)= edit().also(batch).apply()
}