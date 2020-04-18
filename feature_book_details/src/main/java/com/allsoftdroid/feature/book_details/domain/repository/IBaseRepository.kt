package com.allsoftdroid.feature.book_details.domain.repository

import com.allsoftdroid.common.base.extension.Event
import com.allsoftdroid.common.base.extension.Variable
import com.allsoftdroid.feature.book_details.utils.NetworkState

interface IBaseRepository {
    fun networkResponse(): Variable<Event<NetworkState>>
}