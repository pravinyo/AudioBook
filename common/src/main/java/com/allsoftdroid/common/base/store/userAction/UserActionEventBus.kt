package com.allsoftdroid.common.base.store.userAction

import com.allsoftdroid.common.base.extension.Event

class UserActionEventBus {
    companion object{
        fun getEventBusInstance() = UserActionEventStore.getInstance(
            Event(Nothing(this::class.java.simpleName))
        )
    }
}