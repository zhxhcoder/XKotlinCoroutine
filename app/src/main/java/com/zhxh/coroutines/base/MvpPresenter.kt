package com.zhxh.coroutines.base

import android.support.annotation.UiThread

interface MvpPresenter<V : MvpView> {

    @UiThread
    fun subscribe(view: V)

    @UiThread
    fun unsubscribe()
}