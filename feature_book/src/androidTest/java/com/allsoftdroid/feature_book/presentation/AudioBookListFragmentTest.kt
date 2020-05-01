package com.allsoftdroid.feature_book.presentation


import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.MediumTest
import androidx.test.runner.AndroidJUnit4
import com.allsoftdroid.feature_book.R
import com.allsoftdroid.feature_book.di.FeatureBookModule
import com.allsoftdroid.feature_book.presentation.audiobookListFragment.di.bookListViewModelModule
import com.allsoftdroid.feature_book.presentation.audiobookListFragment.di.jobModule
import com.allsoftdroid.feature_book.presentation.audiobookListFragment.di.repositoryModule
import com.allsoftdroid.feature_book.presentation.audiobookListFragment.di.usecaseModule
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

@RunWith(AndroidJUnit4::class)
@MediumTest
class AudioBookListFragmentTest{

    @Before
    fun startKoinForTest() {
//        val application = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext
        startKoin {
        }
    }

    @Before
    fun setup(){
        FeatureBookModule.bookListViewModelModule = bookListViewModelModule
        FeatureBookModule.usecaseModule = usecaseModule
        FeatureBookModule.repositoryModule = repositoryModule
        FeatureBookModule.jobModule = jobModule

//        FeatureBookModule.injectFeature()
    }

    @Test
    fun audioBookList_DisplayedInUI(){
        launchFragmentInContainer<AudioBookListFragment>(themeResId = R.style.AppTheme)
        onView(withId(R.id.item_title)).check(matches(isDisplayed()))
        onView(withId(R.id.item_title)).check(matches(withText("Title")))

        onView(withId(R.id.item_summary)).check(matches(isDisplayed()))
        onView(withId(R.id.item_summary)).check(matches(withSubstring("creator")))
//        Thread.sleep(2000)
    }

    @Test
    fun clickBookItem_navigateToBookDetailsFragmentOne(){
        //Given
        val scenario = launchFragmentInContainer<AudioBookListFragment>(themeResId = R.style.AppTheme)
        var bundle : Bundle = Bundle.EMPTY

        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        //When
        `when`(navController.navigate(R.id.action_AudioBookListFragment_to_AudioBookDetailsFragment,bundle))
            .then{

            }
        onView(withId(R.id.recycler_view_books))
            .perform(
                RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                hasDescendant(withText("Title")), click()))


        scenario.onFragment {
            bundle = it.bundleShared
        }

        //verify(navController).navigate(ArgumentMatchers.eq(R.id.action_AudioBookListFragment_to_AudioBookDetailsFragment),ArgumentMatchers.any())
    }

    @After fun stopKoinAfterTest(){
        FeatureBookModule.unLoadModules()
        stopKoin()
    }
}