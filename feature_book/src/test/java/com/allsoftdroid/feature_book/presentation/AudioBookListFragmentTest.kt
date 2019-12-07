package com.allsoftdroid.feature_book.presentation


import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.ext.junit.runners.AndroidJUnit4
//import androidx.test.filters.MediumTest
import com.allsoftdroid.feature_book.R
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
//@MediumTest
class AudioBookListFragmentTest2{


    @Test
    fun audioBookList_DisplayedInUI(){
        launchFragmentInContainer<AudioBookListFragment>(themeResId = R.style.AppTheme)
    }
}