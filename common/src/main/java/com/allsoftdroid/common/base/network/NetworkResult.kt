package com.allsoftdroid.common.base.network

sealed class NetworkResult


data class Success(val result: Any) : NetworkResult()
data class Failure(val error: Error) : NetworkResult()

