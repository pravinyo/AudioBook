package com.allsoftdroid.feature_book


//
//import androidx.lifecycle.Lifecycle
//import com.allsoftdroid.feature_book.presentation.AudioBookListFragment
//import org.junit.Test
//import org.junit.runner.RunWith
//import java.util.regex.Pattern.matches
//
//@RunWith(AndroidJunit::class)
//class FragmentBookListTest{
//
//    @Test
//    fun testListFragment(){
//        val scenario = launchFragmentInContainer<AudioBookListFragment>()
//
//        scenario.moveToState(Lifecycle.State.RESUMED)
//        scenario.onFragment {
//            fragment ->
//
//            onView(withId(R.id.recycler_view_books))
//                .check(matches(isDisplayed()))
//        }
//
//    }
//}