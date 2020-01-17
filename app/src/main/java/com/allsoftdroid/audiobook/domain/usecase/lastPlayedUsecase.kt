package com.allsoftdroid.audiobook.domain.usecase

import com.allsoftdroid.audiobook.domain.model.LastPlayedTrack
import com.allsoftdroid.common.base.usecase.BaseUseCase
import com.allsoftdroid.feature.book_details.domain.repository.BookDetailsSharedPreferenceRepository

class GetLastPlayedUsecase : BaseUseCase<GetLastPlayedUsecase.RequestValues, GetLastPlayedUsecase.ResponseValues>() {

    override suspend fun executeUseCase(requestValues: RequestValues?) {
        requestValues?.let { request ->
            request.sharedPref.let {
                if(it.bookId().isNotEmpty()){
                    useCaseCallback?.onSuccess(
                        ResponseValues(
                            lastPlayedTrack = LastPlayedTrack(
                                title = it.trackTitle(),
                                position = it.trackPosition(),
                                bookId = it.bookId(),
                                bookName = it.bookName()
                            )
                        )
                    )
                }
            }
        }?:useCaseCallback?.onError(Error("Request cannot be Null"))
    }

    class RequestValues(val sharedPref: BookDetailsSharedPreferenceRepository) : BaseUseCase.RequestValues
    class ResponseValues (val lastPlayedTrack : LastPlayedTrack) : BaseUseCase.ResponseValues
}