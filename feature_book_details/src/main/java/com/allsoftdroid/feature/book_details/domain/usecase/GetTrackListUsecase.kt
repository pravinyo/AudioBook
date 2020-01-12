package com.allsoftdroid.feature.book_details.domain.usecase

import com.allsoftdroid.common.base.extension.Event
import com.allsoftdroid.common.base.usecase.BaseUseCase
import com.allsoftdroid.feature.book_details.data.repository.TrackFormat
import com.allsoftdroid.feature.book_details.domain.repository.AudioBookMetadataRepository
import timber.log.Timber

class GetTrackListUsecase(
    private val metadataRepository: AudioBookMetadataRepository):
    BaseUseCase<GetTrackListUsecase.RequestValues,GetTrackListUsecase.ResponseValues>(){

    override suspend fun executeUseCase(requestValues: RequestValues?) {
        requestValues?.let {
            metadataRepository.loadTrackListData(requestValues.trackFormat)
            Timber.d("fetching started")

            useCaseCallback?.onSuccess(ResponseValues(Event(Unit)))
        }?:useCaseCallback?.onError(Error("Request value should not be null"))
    }

    fun getTrackListData() = metadataRepository.getTrackList()

    class RequestValues(val trackFormat: TrackFormat) : BaseUseCase.RequestValues
    class ResponseValues (val event : Event<Any>) : BaseUseCase.ResponseValues
}