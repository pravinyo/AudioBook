package com.allsoftdroid.database.common

interface SaveInDatabase<T> {

    var mDao : T

    fun addData(data:Any): Any

    suspend fun execute(): Any
}