package com.zhxh.coroutines.net

import android.util.Log
import com.google.gson.Gson
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.CallAdapter
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.util.ArrayList
import java.util.concurrent.TimeUnit

abstract class BaseNetService {

    abstract val baseUrl: String

    abstract val interceptor: Interceptor

    /**
     * 方法描述：加上CallAdapter.Factory
     *
     * @author zhxh
     * @time 2018/6/19
     */
    open fun getCallAdapterFactory(): CallAdapter.Factory {
        return RxJava2CallAdapterFactory.create()
    }

    /**
     * 拦截器列表
     * @author zhxh
     * @return 拦截器列表
     * @time 2019/6/19
     */
    open fun getInterceptorList(): List<Interceptor> {
        return ArrayList()
    }

    val timeout: Long
        get() = 30


    fun <T> createService(clazz: Class<T>): T {
        // 添加日志打印
        val loggingInterceptor = HttpLoggingInterceptor(HttpLoggingInterceptor.Logger { message ->
            try {
                Log.d(
                    "netLog:", URLDecoder.decode(
                        message.replace("%(?![0-9a-fA-F]{2})".toRegex(), "%25")
                            .replace("\\+".toRegex(), "%2B"), "UTF-8"
                    )
                )
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }
        })
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        // 设置okHttpClient
        val okHttpClientBuilder = OkHttpClient.Builder().connectTimeout(
            timeout, TimeUnit
                .SECONDS
        ).readTimeout(timeout, TimeUnit.SECONDS).addInterceptor(interceptor).addInterceptor(loggingInterceptor)

        for (interceptor in getInterceptorList()) {
            okHttpClientBuilder.addInterceptor(interceptor)
        }


        var retrofit = Retrofit.Builder().client(okHttpClientBuilder.build()).baseUrl(baseUrl)
            .addCallAdapterFactory(getCallAdapterFactory())
            .addConverterFactory(GsonConverterFactory.create(Gson())).build()

        return retrofit.create(clazz)
    }
}
