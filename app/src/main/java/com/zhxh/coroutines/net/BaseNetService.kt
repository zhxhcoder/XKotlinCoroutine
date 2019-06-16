package com.zhxh.coroutines.net

import android.util.Log
import com.google.gson.Gson
import com.zhxh.coroutines.model.CoroutineCallAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.util.concurrent.TimeUnit

abstract class BaseNetService {

    abstract val baseUrl: String

    abstract val interceptor: Interceptor
    val timeOut: Long
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
            timeOut, TimeUnit
                .SECONDS
        ).readTimeout(timeOut, TimeUnit.SECONDS).addInterceptor(interceptor).addInterceptor(loggingInterceptor)

        var retrofit = Retrofit.Builder().client(okHttpClientBuilder.build()).baseUrl(baseUrl)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .addConverterFactory(GsonConverterFactory.create(Gson())).build()

        return retrofit.create(clazz)
    }
}
