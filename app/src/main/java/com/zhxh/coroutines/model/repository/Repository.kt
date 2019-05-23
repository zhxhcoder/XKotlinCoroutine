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
                val bResult = ApiSource.instance.getNetDataB().await()
                val aResult = ApiSource.instance.getNetDataA().await()

                val result = mutableListOf<CommonBean>().apply {
                    addAll(aResult.data)
                    addAll(bResult.data)
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
            val bResult = ApiSource.instance.getNetDataB().await()

            val aResult = ApiSource.instance.getNetDataA().await()

            val result = mutableListOf<CommonBean>().apply {
                addAll(aResult.data)
                addAll(bResult.data)
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
                    val bResult = ApiSource.instance.getNetDataB().await()
                    bResult
                }

                val iosDeferred = async {
                    val aResult = ApiSource.instance.getNetDataA().await()
                    aResult
                }

                val bResult = androidDeferred.await().data
                val aResult = iosDeferred.await().data

                val result = mutableListOf<CommonBean>().apply {
                    addAll(aResult)
                    addAll(bResult)
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
                    val bResult = ApiSource.instance.getNetDataB().execute()
                    if (bResult.isSuccessful) {
                        bResult.body()!!
                    } else {
                        throw Throwable("b request failure")
                    }
                }

                val iosDeferred = async {
                    val aResult = ApiSource.instance.getNetDataA().execute()
                    if (aResult.isSuccessful) {
                        aResult.body()!!
                    } else {
                        throw Throwable("a request failure")
                    }
                }

                val bResult = androidDeferred.await().data
                val aResult = iosDeferred.await().data

                val result = mutableListOf<CommonBean>().apply {
                    addAll(aResult)
                    addAll(bResult)
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
                val androidDeferred = ApiSource.callAdapterInstance.getNetDataB()

                val iosDeferred = ApiSource.callAdapterInstance.getNetDataA()

                val bResult = androidDeferred.await().data

                val aResult = iosDeferred.await().data

                val result = mutableListOf<CommonBean>().apply {
                    addAll(aResult)
                    addAll(bResult)
                }
                result
            } catch (e: Throwable) {
                e.printStackTrace()
                throw e
            }
        }
    }
}