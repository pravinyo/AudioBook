package com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.domain.repository

import com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.domain.network.NetworkResponseListener

interface INetworkBaseRepository {
    fun registerNetworkResponse(listener: NetworkResponseListener)
    fun unRegisterNetworkResponse()
}