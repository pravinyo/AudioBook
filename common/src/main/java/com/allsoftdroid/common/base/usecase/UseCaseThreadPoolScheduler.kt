package com.allsoftdroid.common.base.usecase

import android.os.Handler
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

class UseCaseThreadPoolScheduler : BaseUseCaseScheduler {

    private val POOL_SIZE = 2

    private val MAX_POOL_SIZE = 4

    private val TIMEOUT = 30

    private val mHandler = Handler()

    private  var mThreadPoolExecutor : ThreadPoolExecutor

    init {
        mThreadPoolExecutor = ThreadPoolExecutor(POOL_SIZE,MAX_POOL_SIZE,TIMEOUT.toLong(),
            TimeUnit.SECONDS, ArrayBlockingQueue(POOL_SIZE))
    }

    override fun execute(runnable: Runnable)  =  mThreadPoolExecutor.execute(runnable)

    override fun <V : BaseUseCase.ResponseValues> notifyResponse(
        response: V,
        useCaseCallback: BaseUseCase.UseCaseCallback<V>
    ) {
        mHandler.post{useCaseCallback.onSuccess(response)}
    }

    override fun <V : BaseUseCase.ResponseValues> onError(
        useCaseCallback: BaseUseCase.UseCaseCallback<V>,
        t: Throwable
    ) {
        mHandler.post { useCaseCallback.onError(t) }
    }
}