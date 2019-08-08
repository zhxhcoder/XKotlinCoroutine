package com.zhxh.coroutines.base

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel

open class BasePresenter<V : MvpView> : MvpPresenter<V> {
    lateinit var view: V
    internal val presenterScope: CoroutineScope by lazy {
        CoroutineScope(Dispatchers.Main + Job())
    }

    override fun subscribe() {
    }

    override fun unsubscribe() {
        presenterScope.cancel()
    }
}