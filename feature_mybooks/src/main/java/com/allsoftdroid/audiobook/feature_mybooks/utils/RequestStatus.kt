package com.allsoftdroid.audiobook.feature_mybooks.utils

import com.allsoftdroid.audiobook.feature_mybooks.data.model.LocalBookDomainModel

sealed class RequestStatus

data class Success(val list : List<LocalBookDomainModel>) : RequestStatus()
object Empty : RequestStatus()
object Started : RequestStatus()
