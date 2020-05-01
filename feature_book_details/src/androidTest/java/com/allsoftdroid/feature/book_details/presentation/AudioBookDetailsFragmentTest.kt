package com.allsoftdroid.feature.book_details.presentation

import android.app.Application
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
import com.allsoftdroid.common.base.usecase.UseCaseHandler
import com.allsoftdroid.feature.book_details.R
import com.allsoftdroid.feature.book_details.data.repository.BookDetailsSharedPreferencesRepositoryImpl
import com.allsoftdroid.feature.book_details.di.BookDetailsModule
import com.allsoftdroid.feature.book_details.domain.repository.BookDetailsSharedPreferenceRepository
import com.allsoftdroid.feature.book_details.presentation.BookDetailsDI.dataModule
import com.allsoftdroid.feature.book_details.presentation.BookDetailsDI.repositoryModule
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

    @Test
    fun audioBookDetails_DisplayedInUi() {

        // WHEN - Details fragment launched to display task
        val bundle = bundleOf()
        launchFragmentInContainer<AudioBookDetailsFragment>(bundle, R.style.AppTheme)
        Thread.sleep(2000)
    }

    @After
    fun teardown(){
        stopKoin()
    }

}