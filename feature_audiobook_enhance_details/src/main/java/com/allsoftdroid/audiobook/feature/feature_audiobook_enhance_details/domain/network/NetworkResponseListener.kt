package com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.domain.network

import com.allsoftdroid.common.base.network.NetworkResult

interface NetworkResponseListener{
    suspend fun onResponse(result  : NetworkResult)
}