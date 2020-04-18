package com.allsoftdroid.feature.book_details.domain.usecase

import com.allsoftdroid.common.base.extension.Event
import com.allsoftdroid.common.base.store.downloader.DownloadEvent
import com.allsoftdroid.common.base.store.downloader.DownloadEventStore
import com.allsoftdroid.common.base.usecase.BaseUseCase


class GetDownloadUsecase(private val downloadEventStore: DownloadEventStore) :
    BaseUseCase<GetDownloadUsecase.RequestValues, GetDownloadUsecase.ResponseValues>() {

    override suspend fun executeUseCase(requestValues: RequestValues?) {
        //check network connection
        requestValues?.let {
            downloadEventStore.publish(
                Event(it.downloaderAction)
            )
            useCaseCallback?.onSuccess(ResponseValues(Event("Sent")))
        }?:useCaseCallback?.onError(Error("Request is null"))
    }

    class RequestValues(val downloaderAction: DownloadEvent) : BaseUseCase.RequestValues
    class ResponseValues(val event: Event<Any>) : BaseUseCase.ResponseValues
}