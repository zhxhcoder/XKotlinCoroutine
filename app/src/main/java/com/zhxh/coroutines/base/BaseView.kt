package com.zhxh.coroutines.base

import android.support.annotation.UiThread

interface BaseView<T> : MvpView {
    fun initView(data: T)
    @UiThread
    fun loadingIndicator(show: Boolean, msg: String)
}