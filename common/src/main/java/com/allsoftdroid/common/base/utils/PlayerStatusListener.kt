package com.allsoftdroid.common.base.utils

import com.allsoftdroid.common.base.extension.Event

interface PlayerStatusListener {
    fun onPlayerStatusChange(shouldShow : Event<Boolean>)
}