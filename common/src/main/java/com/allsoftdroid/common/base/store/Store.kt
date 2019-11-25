package com.allsoftdroid.common.base.store


import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.Relay
import io.reactivex.Observable


open class Store<T>(defaultValue: T) {

    private val storeSubject: Relay<T> = BehaviorRelay.createDefault(defaultValue).toSerialized()

    fun observe(): Observable<T> {
        return storeSubject.hide()
            .distinctUntilChanged()
    }

    fun publish(value: T) {
        storeSubject.accept(value)
    }
}