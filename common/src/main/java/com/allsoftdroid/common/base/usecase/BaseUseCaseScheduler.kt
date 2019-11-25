package com.allsoftdroid.common.base.usecase

interface BaseUseCaseScheduler {

    fun execute(runnable: Runnable)

    fun < V :  BaseUseCase.ResponseValues> notifyResponse(
        response : V,
        useCaseCallback: BaseUseCase.UseCaseCallback<V>
    )

    fun <V : BaseUseCase.ResponseValues> onError(
        useCaseCallback: BaseUseCase.UseCaseCallback<V>,
        t : Throwable
    )
}