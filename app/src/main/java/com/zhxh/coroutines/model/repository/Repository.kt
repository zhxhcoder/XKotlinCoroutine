package com.zhxh.coroutines.model.repository

import com.zhxh.coroutines.model.ApiSource
import com.zhxh.coroutines.model.await
import com.zhxh.coroutines.entities.CommonBean
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

const val TAG = "TestCoroutine"
object Repository {

    /**
     * 两个请求在子线程中顺序执行，非同时并发
     */
    suspend fun querySyncWithContext(): List<CommonBean> {
        return withContext(Dispatchers.Main) {
            try {
                val androidResult = ApiSource.instance.getSK2().await()

                val iosResult = ApiSource.instance.getSK1().await()

                val result = mutableListOf<CommonBean>().apply {
                    addAll(iosResult.results)
                    addAll(androidResult.results)
                }
                result
            } catch (e: Throwable) {
                e.printStackTrace()
                throw e
            }
        }
    }

    /**
     * 两个请求在主线程中顺序执行，非同时并发
     */
    suspend fun querySyncNoneWithContext(): List<CommonBean> {
        return try {
            val androidResult = ApiSource.instance.getSK2().await()

            val iosResult = ApiSource.instance.getSK1().await()

            val result = mutableListOf<CommonBean>().apply {
                addAll(iosResult.results)
                addAll(androidResult.results)
            }
            result
        } catch (e: Throwable) {
            e.printStackTrace()
            throw e
        }
    }

    /**
     * 两个请求在子线程中并发执行
     */
    suspend fun queryAsyncWithContextForAwait(): List<CommonBean> {
        return withContext(Dispatchers.Main) {
            try {
                val androidDeferred = async {
                    val androidResult = ApiSource.instance.getSK2().await()
                    androidResult
                }

                val iosDeferred = async {
                    val iosResult = ApiSource.instance.getSK1().await()
                    iosResult
                }

                val androidResult = androidDeferred.await().results
                val iosResult = iosDeferred.await().results

                val result = mutableListOf<CommonBean>().apply {
                    addAll(iosResult)
                    addAll(androidResult)
                }
                result
            } catch (e: Throwable) {
                e.printStackTrace()
                throw e
            }
        }
    }

    /**
     * 两个请求在子线程中并发执行
     */
    suspend fun queryAsyncWithContextForNoAwait(): List<CommonBean> {
        return withContext(Dispatchers.Main) {
            try {
                val androidDeferred = async {
                    val androidResult = ApiSource.instance.getSK2().execute()
                    if(androidResult.isSuccessful) {
                        androidResult.body()!!
                    } else {
                        throw Throwable("android request failure")
                    }
                }

                val iosDeferred = async {
                    val iosResult = ApiSource.instance.getSK1().execute()
                    if(iosResult.isSuccessful) {
                        iosResult.body()!!
                    } else {
                        throw Throwable("ios request failure")
                    }
                }

                val androidResult = androidDeferred.await().results
                val iosResult = iosDeferred.await().results

                val result = mutableListOf<CommonBean>().apply {
                    addAll(iosResult)
                    addAll(androidResult)
                }
                result
            } catch (e: Throwable) {
                e.printStackTrace()
                throw e
            }
        }
    }

    suspend fun adapterCoroutineQuery(): List<CommonBean> {
        return withContext(Dispatchers.Main) {
            try {
                val androidDeferred = ApiSource.callAdapterInstance.getSK2()

                val iosDeferred = ApiSource.callAdapterInstance.getSK1()

                val androidResult = androidDeferred.await().results

                val iosResult = iosDeferred.await().results

                val result = mutableListOf<CommonBean>().apply {
                    addAll(iosResult)
                    addAll(androidResult)
                }
                result
            } catch (e: Throwable) {
                e.printStackTrace()
                throw e
            }
        }
    }
}