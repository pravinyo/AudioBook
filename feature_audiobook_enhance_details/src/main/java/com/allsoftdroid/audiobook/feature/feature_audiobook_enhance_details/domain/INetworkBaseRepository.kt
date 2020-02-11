package com.allsoftdroid.audiobook.feature.feature_audiobook_enhance_details.domain

interface INetworkBaseRepository {
    fun registerNetworkResponse(listener: NetworkResponseListener)
    fun unRegisterNetworkResponse()
}