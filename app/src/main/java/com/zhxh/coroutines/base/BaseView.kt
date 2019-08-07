package com.zhxh.coroutines.base

import android.support.annotation.UiThread

interface BaseView<T> : MvpView {
    @UiThread
    fun loadingIndicator(show: Boolean, msg: String)
}