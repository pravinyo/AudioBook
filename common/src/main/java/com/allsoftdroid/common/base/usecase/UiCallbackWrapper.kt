package com.allsoftdroid.common.base.usecase

class UiCallbackWrapper< V : BaseUseCase.ResponseValues>(
    private val callback: BaseUseCase.UseCaseCallback<V>,
    private val mUseCaseHandler: UseCaseHandler) : BaseUseCase.UseCaseCallback<V> {



    override fun onSuccess(response: V) {
        mUseCaseHandler.notifyResponse(response,callback)
    }

    override fun onError(t: Throwable) {
        mUseCaseHandler.notifyError(callback,t)
    }

    companion object{

        private var INSTANCE : UseCaseHandler ? = null

        fun getInstance() : UseCaseHandler{

            if(INSTANCE == null){
                INSTANCE = UseCaseHandler(UseCaseThreadPoolScheduler())
            }

            return INSTANCE!!
        }
    }
}
