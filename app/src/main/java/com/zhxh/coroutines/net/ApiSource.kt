package com.zhxh.coroutines.net

import android.util.Log
import com.zhxh.coroutines.entities.CommonBean
import com.zhxh.coroutines.entities.CommonResult
import io.reactivex.Observable
import kotlinx.coroutines.Deferred
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Created by zhxh on 2019/05/19
 *
 * http://www.weather.com.cn/data/sk/101110102.html
 */
interface DeferredApiService {
    @GET("zhxh/list")
    fun getNetDataA(): Deferred<CommonResult>

    @GET("zhxh/array")
    fun getNetDataB(): Deferred<CommonResult>
}

interface CallAPIService {
    @GET("zhxh/list")
    fun getNetDataA(): Call<CommonResult>

    @GET("zhxh/array")
    fun getNetDataB(): Call<CommonResult>
}

//Observable
interface RxJavaApiService {
    @GET("zhxh/list")
    fun getNetDataA(): Observable<CommonResult>

    @GET("zhxh/array")
    fun getNetDataB(): Deferred<CommonResult>
}


class ApiSource {

    companion object {
        // 添加日志打印
        private val loggingInterceptor = HttpLoggingInterceptor(HttpLoggingInterceptor.Logger { message ->
            try {
                Log.i(
                    "netLog:", URLDecoder.decode(
                        message.replace("%(?![0-9a-fA-F]{2})".toRegex(), "%25")
                            .replace("\\+".toRegex(), "%2B"), "UTF-8"
                    )
                )
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }
        })
        private val okHttpClientBuilder: OkHttpClient.Builder = OkHttpClient.Builder().connectTimeout(
            30, TimeUnit
                .SECONDS
        ).readTimeout(30, TimeUnit.SECONDS).addInterceptor(loggingInterceptor)
            .addInterceptor(loggingInterceptor)


        @JvmField
        val deferredInstance = Retrofit.Builder()
            .baseUrl("https://www.easy-mock.com/mock/5c10abcd8c59f04d2e3a7722/")
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(DeferredApiService::class.java)

        @JvmField
        val rxjavaInstance = Retrofit.Builder().client(okHttpClientBuilder.build())
            .baseUrl("https://www.easy-mock.com/mock/5c10abcd8c59f04d2e3a7722/")
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(RxJavaApiService::class.java)

        @JvmField
        val callInstance = Retrofit.Builder()
            .baseUrl("https://www.easy-mock.com/mock/5c10abcd8c59f04d2e3a7722/")
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(CallAPIService::class.java)
    }
}

suspend fun <T> Call<T>.await(): T {
    return suspendCoroutine {
        enqueue(object : Callback<T> {
            override fun onFailure(call: Call<T>, t: Throwable) {
                it.resumeWithException(t)
            }

            override fun onResponse(call: Call<T>, response: Response<T>) {
                if (response.isSuccessful) {
                    it.resume(response.body()!!)
                } else {
                    it.resumeWithException(Throwable(response.toString()))
                }
            }
        })
    }
}

suspend fun <T> Observable<T>.await(): T {
    return suspendCoroutine {

    }
}