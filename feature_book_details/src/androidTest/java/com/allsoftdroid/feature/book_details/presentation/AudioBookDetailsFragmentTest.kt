package com.allsoftdroid.feature.book_details.presentation

import android.app.Application
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
import com.allsoftdroid.common.base.store.audioPlayer.AudioPlayerEventBus
import com.allsoftdroid.common.base.store.audioPlayer.PlaySelectedTrack
import com.allsoftdroid.feature.book_details.R
import com.allsoftdroid.feature.book_details.data.repository.BookDetailsSharedPreferencesRepositoryImpl
import com.allsoftdroid.feature.book_details.di.BookDetailsModule
import com.allsoftdroid.feature.book_details.domain.repository.BookDetailsSharedPreferenceRepository
import com.allsoftdroid.feature.book_details.presentation.BookDetailsDI.dataModule
import com.allsoftdroid.feature.book_details.presentation.BookDetailsDI.repositoryModule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.Matchers.`is`
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.module.Module
import org.koin.dsl.module

@MediumTest
@RunWith(AndroidJUnit4::class)
class AudioBookDetailsFragmentTest{

    @Before
    fun setup(){

        val application = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application
        val storageModule : Module = module {
            single<BookDetailsSharedPreferenceRepository> {
                BookDetailsSharedPreferencesRepositoryImpl.create(application)
            }
        }

        BookDetailsModule.dataModule = dataModule
        BookDetailsModule.repositoryModule = repositoryModule

        startKoin {
            modules(storageModule)
        }
    }

    @ExperimentalCoroutinesApi
    @Test
    fun audioBookDetails_DisplayedInUi() {
        runBlockingTest {
            //Given
            val title = "The Art of War"
            val bundle = bundleOf()
            bundle.putString("bookId","art_of_war_librivox")
            bundle.putString("title",title)

            //action - Details fragment launched to display task
            launchFragmentInContainer<AudioBookDetailsFragment>(bundle, R.style.AppTheme)

            //Assert
            onView(withId(R.id.tv_toolbar_title)).check(matches(isDisplayed()))
            onView(withId(R.id.tv_toolbar_title)).check(matches(withText(title)))

            onView(withId(R.id.tv_book_desc_text)).check(matches(isDisplayed()))

            onView(withId(R.id.btn_toolbar_back_arrow)).check(matches(isDisplayed()))
            onView(withId(R.id.btn_toolbar_back_arrow)).check(matches(isClickable()))
            onView(withId(R.id.btn_toolbar_back_arrow)).check(matches(isEnabled()))

            onView(withId(R.id.recyclerView)).check(matches(isDisplayed()))
            onView(withId(R.id.recyclerView)).check(matches(hasChildCount(2)))
        }
    }

    @Test
    fun clickChapter_playEventSent(){
        //Given
        val title = "The Art of War"
        val bundle = bundleOf()
        bundle.putString("bookId","art_of_war_librivox")
        bundle.putString("title",title)

        // WHEN - Details fragment launched to display task
        launchFragmentInContainer<AudioBookDetailsFragment>(bundle, R.style.AppTheme)

        onView(withId(R.id.recyclerView))
            .perform(RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                hasDescendant(withText("sample2 track")),click()
            ))

        //Assert
        val store = AudioPlayerEventBus.getEventBusInstance()
        store.observe().subscribe {
            val type = when(it.peekContent()){
                is PlaySelectedTrack -> "PlaySelectedTrack"
                else -> "Not PlaySelectedTrack"
            }

            assertThat(type,`is`("PlaySelectedTrack"))
        }
    }

    @After
    fun teardown(){
        stopKoin()
    }

}