package com.allsoftdroid.feature.book_details.domain.usecase

import com.allsoftdroid.common.base.extension.Event
import com.allsoftdroid.common.base.usecase.BaseUseCase
import com.allsoftdroid.feature.book_details.data.model.TrackFormat
import com.allsoftdroid.feature.book_details.domain.repository.ITrackListRepository
import timber.log.Timber

class GetTrackListUsecase(
    private val listRepository: ITrackListRepository):
    BaseUseCase<GetTrackListUsecase.RequestValues,GetTrackListUsecase.ResponseValues>(){

    public override suspend fun executeUseCase(requestValues: RequestValues?) {
        requestValues?.let {
            listRepository.loadTrackListData(requestValues.trackFormat)
            Timber.d("fetching started")

            useCaseCallback?.onSuccess(ResponseValues(Event(Unit)))
        }?:useCaseCallback?.onError(Error("Request value should not be null"))
    }

    fun getTrackListData() = listRepository.getTrackList()

    class RequestValues(val trackFormat: TrackFormat) : BaseUseCase.RequestValues
    class ResponseValues (val event : Event<Any>) : BaseUseCase.ResponseValues
}