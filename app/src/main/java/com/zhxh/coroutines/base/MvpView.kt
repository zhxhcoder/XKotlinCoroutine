package com.zhxh.coroutines.base

interface MvpView {
    fun <T> initView(data: T)
    fun destroyView()
}