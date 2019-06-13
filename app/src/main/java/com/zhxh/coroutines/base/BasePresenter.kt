package com.zhxh.coroutines.base

import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers

open class BasePresenter<V : MvpView> : MvpPresenter<V> {
    lateinit var view: V
    internal val presenterScope: CoroutineScope by lazy {
        CoroutineScope(Dispatchers.Main + Job())
    }

    override fun subscribe(view: V) {
        this.view = view
    }

    override fun unsubscribe() {
        presenterScope.cancel()
    }
}