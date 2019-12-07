package com.allsoftdroid.audiobook


import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
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
        onView(withId(R.id.item_title)).check(matches(isDisplayed()))
        onView(withId(R.id.item_title)).check(matches(withText("Title")))

        onView(withId(R.id.item_summary)).check(matches(isDisplayed()))
        onView(withId(R.id.item_summary)).check(matches(withSubstring("creator")))
        Thread.sleep(2000)
    }

    @After
    fun tearDown(){
        FeatureBookModule.unLoadModules()
    }
}