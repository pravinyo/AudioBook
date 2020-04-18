package com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.domain.repository

import com.dropbox.android.external.store4.Store

interface IStoreRepository {

    fun provideStoreMultiParam(): Store<Pair<String, Int>, String>
}