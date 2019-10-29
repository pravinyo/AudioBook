package com.allsoftdroid.feature.book_details.domain.usecase

import com.allsoftdroid.common.base.extension.Event
import com.allsoftdroid.common.base.usecase.BaseUseCase
import com.allsoftdroid.feature.book_details.domain.repository.AudioBookMetadataRepository
import timber.log.Timber

class GetMetadataUsecase( private val metadataRepository: AudioBookMetadataRepository) :
    BaseUseCase<GetMetadataUsecase.RequestValues, GetMetadataUsecase.ResponseValues>(){

    override suspend fun executeUseCase(requestValues: RequestValues?) {

        metadataRepository.loadMetadata()
        Timber.d("fetching started")

        val responseValues = ResponseValues(Event(Unit))
        useCaseCallback?.onSuccess(responseValues)
    }

    fun getMetadata() = metadataRepository.getMetadata()

    fun getBookIdentifier() = metadataRepository.getBookId()

    class RequestValues(val bookId : String) : BaseUseCase.RequestValues
    class ResponseValues (val event : Event<Any>) : BaseUseCase.ResponseValues
}