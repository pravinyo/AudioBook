package com.allsoftdroid.audiobook.presentation

import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.NavigationViewActions
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.allsoftdroid.audiobook.R
import com.allsoftdroid.common.test.DataBindingIdlingResource
import com.allsoftdroid.common.test.EspressoIdlingResource
import com.allsoftdroid.common.test.monitorActivity
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class MainActivityTest{

    private val dataBindingIdlingResource = DataBindingIdlingResource()

    @Before
    fun registerIdlingResource(){
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    @After
    fun unRegisterIdlingResource(){
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    @Test
    fun openDownloadsActivity_DisplayInUi(){
        val activityScenario = ActivityScenario.launch(MainActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        onView(withId(R.id.toolbar_title)).check(matches(withText("Latest Audio Books")))

        onView(withId(R.id.toolbar_nav_hamburger)).perform(click())

        onView(withId(R.id.nav_view))
            .perform(NavigationViewActions.navigateTo(R.id.nav_item_downloads))

        pressBack()

        activityScenario.close()
    }

    @Test
    fun searchBook_DisplayInUi(){
        val activityScenario = ActivityScenario.launch(MainActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        onView(withId(R.id.toolbar_book_search)).check(matches(isDisplayed()))
        onView(withId(R.id.toolbar_book_search)).perform(click())

        onView(withId(R.id.iv_search_cancel)).check(matches(isDisplayed()))
        onView(withId(R.id.iv_search_cancel)).perform(click())

        onView(withId(R.id.toolbar_title)).check(matches(withText("Latest Audio Books")))

        onView(withId(R.id.toolbar_book_search)).check(matches(isDisplayed()))
        onView(withId(R.id.toolbar_book_search)).perform(click())

        onView(withId(R.id.et_toolbar_search)).check(matches(isDisplayed()))
        onView(withId(R.id.et_toolbar_search)).perform(clearText(), typeText("poem"))

        onView(withId(R.id.iv_search)).check(matches(isDisplayed()))
        onView(withId(R.id.iv_search)).perform(click())


        Thread.sleep(4000)
        onView(withId(R.id.recycler_view_books))
            .check(matches(hasMinimumChildCount(1)))

        activityScenario.close()
    }

    @Test
    fun openBookDetailsScreen_DisplayInUi(){
        val activityScenario = ActivityScenario.launch(MainActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        Thread.sleep(10000)
        onView(withId(R.id.recycler_view_books))
            .perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(0,click()))

        onView(withId(R.id.tv_toolbar_title)).check(matches(isDisplayed()))

        onView(withId(R.id.btn_toolbar_back_arrow)).check(matches(isDisplayed()))

        onView(withId(R.id.textView_book_intro)).check(matches(isDisplayed()))

        pressBack()

        activityScenario.close()
    }

    @Test
    fun scroll_down_openBookDetailsScreen_DisplayInUi(){
        val activityScenario = ActivityScenario.launch(MainActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        Thread.sleep(10000)
        onView(withId(R.id.recycler_view_books))
            .perform(scrollToPosition<RecyclerView.ViewHolder>(25))
            .perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(25,click()))


        pressBack()

        activityScenario.close()
    }

    @Test
    fun play_Book_from_BookDetailsScreen_DisplayInUi(){
        val activityScenario = ActivityScenario.launch(MainActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        Thread.sleep(10000)
        onView(withId(R.id.recycler_view_books))
            .perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(0,click()))

        onView(withId(R.id.tv_toolbar_title)).check(matches(isDisplayed()))

        onView(withId(R.id.btn_toolbar_back_arrow)).check(matches(isDisplayed()))

        onView(withId(R.id.textView_book_intro)).check(matches(isDisplayed()))

        Thread.sleep(4000)
        onView(withId(R.id.recyclerView))
            .perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(0,click()))


        Thread.sleep(2000)
        onView(withId(R.id.miniPlayerContainer)).check(matches(isDisplayed()))


        activityScenario.close()
    }
}