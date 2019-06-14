package com.zhxh.coroutines.model

import com.zhxh.coroutines.entities.CommonResult
import kotlinx.coroutines.Deferred
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
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

interface ApiService {
    @GET("zhxh/list")
    fun getNetDataA(): Call<CommonResult>

    @GET("zhxh/array")
    fun getNetDataB(): Call<CommonResult>
}

/*
//Observable
interface RxJavaApiService {
    @GET("zhxh/list")
    fun getNetDataA(): Observable<CommonResult>

    @GET("zhxh/array")
    fun getNetDataB(): Observable<CommonResult>
}*/

class ApiSource {
    companion object {
        @JvmField
        val deferredInstance = Retrofit.Builder()
            .baseUrl("https://www.easy-mock.com/mock/5c10abcd8c59f04d2e3a7722/")
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(DeferredApiService::class.java)

        @JvmField
        val instance = Retrofit.Builder()
            .baseUrl("https://www.easy-mock.com/mock/5c10abcd8c59f04d2e3a7722/")
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(ApiService::class.java)
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