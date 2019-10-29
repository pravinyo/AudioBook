package com.allsoftdroid.feature_book.domain.repository

import com.allsoftdroid.common.base.network.NetworkResult

interface NetworkResponseListener{
    suspend fun onResponse(result  : NetworkResult)
}