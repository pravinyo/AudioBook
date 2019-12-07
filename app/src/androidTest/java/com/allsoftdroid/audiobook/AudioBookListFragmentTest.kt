package com.allsoftdroid.audiobook


import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.filters.MediumTest
import androidx.test.runner.AndroidJUnit4
import com.allsoftdroid.audiobook.audiobookListFragment.di.bookListViewModelModule
import com.allsoftdroid.audiobook.audiobookListFragment.di.jobModule
import com.allsoftdroid.audiobook.audiobookListFragment.di.repositoryModule
import com.allsoftdroid.audiobook.audiobookListFragment.di.usecaseModule
import com.allsoftdroid.feature_book.R
import com.allsoftdroid.feature_book.di.FeatureBookModule
import com.allsoftdroid.feature_book.presentation.AudioBookListFragment
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@MediumTest
class AudioBookListFragmentTest{

    @Before
    fun setup(){
        FeatureBookModule.bookListViewModelModule = bookListViewModelModule
        FeatureBookModule.usecaseModule = usecaseModule
        FeatureBookModule.repositoryModule = repositoryModule
        FeatureBookModule.jobModule = jobModule
    }

    @Test
    fun audioBookList_DisplayedInUI(){
        launchFragmentInContainer<AudioBookListFragment>(themeResId = R.style.AppTheme)
        Thread.sleep(2000)
    }

    @After
    fun tearDown(){
        FeatureBookModule.unLoadModules()
    }
}