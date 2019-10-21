package com.allsoftdroid.common.base.usecase

abstract class BaseUseCase<Q : BaseUseCase.RequestValues,P : BaseUseCase.ResponseValues> {

    var requestValues : Q? = null
    var useCaseCallback : UseCaseCallback<P>? = null


    internal fun run(){
        executeUseCase(requestValues)
    }

    protected abstract fun executeUseCase(requestValues : Q?)

    /**
     * Data passed  to a request
     */
    interface RequestValues

    /**
     * Data received from a request
     */
    interface  ResponseValues


    interface UseCaseCallback<R>{
        fun onSuccess(response : R)
        fun onError(t : Throwable)
    }
}