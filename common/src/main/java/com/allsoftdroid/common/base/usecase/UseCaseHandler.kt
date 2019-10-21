package com.allsoftdroid.common.base.usecase

class UseCaseHandler(private val mUseCaseScheduler: BaseUseCaseScheduler) {

    fun <T : BaseUseCase.RequestValues, R : BaseUseCase.ResponseValues> execute(
        useCase: BaseUseCase<T,R>, values : T , callback: BaseUseCase.UseCaseCallback<R>
    ){
        useCase.requestValues = values
        useCase.useCaseCallback = UiCallbackWrapper(callback,this)

        mUseCaseScheduler.execute(Runnable {
            useCase.run()
        })
    }

    fun <V : BaseUseCase.ResponseValues> notifyResponse(
        response : V ,
        useCaseCallback: BaseUseCase.UseCaseCallback<V>)
    {
        mUseCaseScheduler.notifyResponse(response,useCaseCallback)
    }

    fun < V : BaseUseCase.ResponseValues> notifyError(
        useCaseCallback: BaseUseCase.UseCaseCallback<V> ,
        t : Throwable)
    {
        mUseCaseScheduler.onError(useCaseCallback,t)
    }
}
