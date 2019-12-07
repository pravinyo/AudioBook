package com.allsoftdroid.audiobook


import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.filters.MediumTest
import androidx.test.runner.AndroidJUnit4
import com.allsoftdroid.feature_book.R
import com.allsoftdroid.feature_book.presentation.AudioBookListFragment
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@MediumTest
class AudioBookListFragmentTest{


    @Test
    fun audioBookList_DisplayedInUI(){
        launchFragmentInContainer<AudioBookListFragment>(themeResId = R.style.AppTheme)
        Thread.sleep(2000)
    }
}