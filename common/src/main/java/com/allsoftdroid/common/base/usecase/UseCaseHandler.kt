package com.allsoftdroid.common.base.usecase

class UseCaseHandler(/*private val mUseCaseScheduler: BaseUseCaseScheduler*/) {

    suspend fun <T : BaseUseCase.RequestValues, R : BaseUseCase.ResponseValues> execute(
        useCase: BaseUseCase<T,R>, values : T , callback: BaseUseCase.UseCaseCallback<R>
    ){
        useCase.requestValues = values
        useCase.useCaseCallback = UiCallbackWrapper(callback,this)

        useCase.run()

    }

    suspend fun <V : BaseUseCase.ResponseValues> notifyResponse(
        response : V ,
        useCaseCallback: BaseUseCase.UseCaseCallback<V>)
    {
        useCaseCallback.onSuccess(response)
    }

    suspend fun < V : BaseUseCase.ResponseValues> notifyError(
        useCaseCallback: BaseUseCase.UseCaseCallback<V> ,
        t : Throwable)
    {
        useCaseCallback.onError(t)
    }

    companion object{

        private var INSTANCE : UseCaseHandler ? = null

        fun getInstance() : UseCaseHandler{

            if(INSTANCE == null){
                INSTANCE = UseCaseHandler()
            }

            return INSTANCE!!
        }
    }
}
