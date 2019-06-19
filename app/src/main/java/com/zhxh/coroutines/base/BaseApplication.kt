package com.zhxh.coroutines.base

import android.app.Application

/**
 * Created by zhxh on 2019/06/19
 */
class BaseApplication : Application() {

    companion object {
        lateinit var instance: Application
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}
