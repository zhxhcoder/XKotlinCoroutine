package com.zhxh.coroutines.model

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
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
interface CallAdapterApiService {
    @GET("data/sk/101110102.html")
    fun getSK1(): Deferred<CommonResult>

    @GET("data/sk/101110101.html")
    fun getSK2(): Deferred<CommonResult>
}

interface ApiService {
    @GET("data/sk/101110102.html")
    fun getSK1(): Call<CommonResult>

    @GET("data/sk/101110101.html")
    fun getSK2(): Call<CommonResult>
}

class ApiSource {
    companion object {
        @JvmField
        val callAdapterInstance = Retrofit.Builder()
            .baseUrl("http://www.weather.com.cn/")
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(CallAdapterApiService::class.java)

        @JvmField
        val instance = Retrofit.Builder()
            .baseUrl("http://www.weather.com.cn/")
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
                if(response.isSuccessful) {
                    it.resume(response.body()!!)
                } else{
                    it.resumeWithException(Throwable(response.toString()))
                }
            }
        })
    }
}