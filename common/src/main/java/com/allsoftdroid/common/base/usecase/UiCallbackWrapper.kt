package com.allsoftdroid.common.base.usecase

class UiCallbackWrapper< V : BaseUseCase.ResponseValues>(
    private val callback: BaseUseCase.UseCaseCallback<V>,
    private val mUseCaseHandler: UseCaseHandler) : BaseUseCase.UseCaseCallback<V> {



    override suspend fun onSuccess(response: V) {
        mUseCaseHandler.notifyResponse(response,callback)
    }

    override suspend fun onError(t: Throwable) {
        mUseCaseHandler.notifyError(callback,t)
    }
}
