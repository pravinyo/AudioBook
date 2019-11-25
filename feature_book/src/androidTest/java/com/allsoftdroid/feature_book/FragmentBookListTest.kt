package com.allsoftdroid.feature_book


import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.runner.AndroidJUnit4
import com.allsoftdroid.feature_book.presentation.AudioBookListFragment
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FragmentBookListTest{

    @Test
    fun testListFragment(){
        val scenario = launchFragmentInContainer<AudioBookListFragment>()

        scenario.moveToState(Lifecycle.State.RESUMED)
        scenario.onFragment {
            fragment ->

            onView(withId(R.id.recycler_view_books))
                .check(matches(isDisplayed()))
        }

    }
}