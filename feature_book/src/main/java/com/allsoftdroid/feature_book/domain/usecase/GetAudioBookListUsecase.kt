package com.allsoftdroid.feature_book.domain.usecase

import com.allsoftdroid.common.base.extension.Event
import com.allsoftdroid.common.base.usecase.BaseUseCase
import com.allsoftdroid.feature_book.domain.repository.AudioBookRepository
import timber.log.Timber

//class GetAudioBookListUsecase(
//    private val audioBookRep: AudioBookRepository
//) {
//    suspend fun execute() = audioBookRep.searchAudioBooks(page = 1)
//
//    fun getAudioBook() = audioBookRep.getAudioBooks()
//}
//


class GetAudioBookListUsecase( private val audioBookRep: AudioBookRepository) :
    BaseUseCase<GetAudioBookListUsecase.RequestValues,GetAudioBookListUsecase.ResponseValues>(){

    override suspend fun executeUseCase(requestValues: RequestValues?) {

        Timber.d("Request received data=${requestValues?.pageNumber?:-1}")
        val pageNumber = requestValues?.pageNumber?:1


        audioBookRep.searchAudioBooks(page = pageNumber)
        Timber.d("fetching started")

        val responseValues = ResponseValues(Event(Unit))
        useCaseCallback?.onSuccess(responseValues)
    }

    fun getBookList() = audioBookRep.getAudioBooks()

    class RequestValues(val pageNumber : Int) : BaseUseCase.RequestValues
    class ResponseValues (val event : Event<Any>) : BaseUseCase.ResponseValues
}