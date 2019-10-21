package com.allsoftdroid.common.base.usecase

abstract class BaseUseCase<Q : BaseUseCase.RequestValues,P : BaseUseCase.ResponseValues> {

    var requestValues : Q? = null
    var useCaseCallback : UseCaseCallback<P>? = null


    internal suspend fun run(){
        executeUseCase(requestValues)
    }

    protected abstract suspend fun executeUseCase(requestValues : Q?)

    /**
     * Data passed  to a request
     */
    interface RequestValues

    /**
     * Data received from a request
     */
    interface  ResponseValues


    interface UseCaseCallback<R>{
        suspend fun onSuccess(response : R)
        suspend fun onError(t : Throwable)
    }
}