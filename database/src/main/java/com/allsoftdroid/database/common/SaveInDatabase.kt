package com.allsoftdroid.database.common

interface SaveInDatabase<T,R> {

    var mDao : T

    fun addData(data:Any): R

    suspend fun execute(): Any
}