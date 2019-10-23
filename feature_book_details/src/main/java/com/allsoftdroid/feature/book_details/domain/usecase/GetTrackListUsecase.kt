package com.allsoftdroid.feature.book_details.domain.usecase

import com.allsoftdroid.common.base.extension.Event
import com.allsoftdroid.common.base.usecase.BaseUseCase
import com.allsoftdroid.feature.book_details.domain.repository.AudioBookMetadataRepository
import timber.log.Timber

class GetTrackListUsecase(
    private val metadataRepository: AudioBookMetadataRepository):
    BaseUseCase<GetTrackListUsecase.RequestValues,GetTrackListUsecase.ResponseValues>(){

    override suspend fun executeUseCase(requestValues: RequestValues?) {
        metadataRepository.loadTrackListData()
        Timber.d("fetching started")

        useCaseCallback?.onSuccess(ResponseValues(Event(Unit)))
    }

    fun getTrackListData() = metadataRepository.getTrackList()

    class RequestValues(val bookId : String) : BaseUseCase.RequestValues
    class ResponseValues (val event : Event<Any>) : BaseUseCase.ResponseValues
}