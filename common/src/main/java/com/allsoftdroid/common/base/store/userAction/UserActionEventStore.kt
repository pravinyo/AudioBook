package com.allsoftdroid.common.base.store.userAction

import com.allsoftdroid.common.base.extension.Event
import com.allsoftdroid.common.base.store.Store
import com.allsoftdroid.common.base.utils.SingletonHolder

class UserActionEventStore private constructor(defaultValue: Event<UserActionEvent>)
    : Store<Event<UserActionEvent>>(defaultValue){
    companion object : SingletonHolder<UserActionEventStore, Event<UserActionEvent>>(creator = ::UserActionEventStore)
}