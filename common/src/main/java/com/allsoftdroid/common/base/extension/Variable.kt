package com.allsoftdroid.common.base.extension

import io.reactivex.subjects.BehaviorSubject

class Variable<T>(defaultValue: T) {
    var value: T = defaultValue
        set(value) {
            field = value
            observable.onNext(value)
        }

    val observable = BehaviorSubject.createDefault(value)
}