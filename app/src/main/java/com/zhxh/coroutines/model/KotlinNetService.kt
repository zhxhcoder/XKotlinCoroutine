package com.zhxh.coroutines.model

import com.creditease.netspy.NetSpyInterceptor
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.zhxh.coroutines.base.BaseApplication
import com.zhxh.coroutines.net.BaseNetService
import okhttp3.Interceptor
import retrofit2.CallAdapter


class KotlinNetService : BaseNetService() {
    //覆盖时，不覆盖时
    override fun getCallAdapterFactory(): CallAdapter.Factory {
        return CoroutineCallAdapterFactory()
    }

    override fun getInterceptorList(): List<Interceptor> {
        return listOf(NetSpyInterceptor(BaseApplication.instance), StethoInterceptor())
    }

    override val baseUrl: String
        get() = "https://www.easy-mock.com/mock/5c10abcd8c59f04d2e3a7722/"


    override val interceptor: Interceptor
        get() = KotlinNetInterceptor()
}
