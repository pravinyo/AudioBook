package com.allsoftdroid.audiobook.feature_listen_later_ui.domain

import com.allsoftdroid.audiobook.feature_listen_later_ui.data.model.ListenLaterItemDomainModel

sealed class RequestStatus

data class Success(val list : List<ListenLaterItemDomainModel>) : RequestStatus()
object Empty : RequestStatus()
object Started : RequestStatus()