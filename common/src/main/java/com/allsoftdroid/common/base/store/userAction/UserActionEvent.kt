package com.allsoftdroid.common.base.store.userAction

sealed class UserActionEvent {
    abstract val source:String
}

data class OpenDownloadUI(
    override val source: String
):UserActionEvent()

data class OpenLicensesUI(
    override val source: String
):UserActionEvent()

data class Nothing(
    override val source: String
):UserActionEvent()

data class OpenMainPlayerUI(
    override val source: String
):UserActionEvent()

data class OpenMiniPlayerUI(
    override val source: String
):UserActionEvent()