package com.zhxh.coroutines.model

import com.zhxh.coroutines.entities.CommonBean
import com.zhxh.coroutines.net.ApiSource
import com.zhxh.coroutines.net.await
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
                val bResult = ApiSource.callInstance.getNetDataB().await()
                val aResult = ApiSource.callInstance.getNetDataA().await()

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
            val bResult = ApiSource.callInstance.getNetDataB().await()

            val aResult = ApiSource.callInstance.getNetDataA().await()

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
                val bDeferred = async {
                    val bResult = ApiSource.callInstance.getNetDataB().await()
                    bResult
                }

                val aDeferred = async {
                    val aResult = ApiSource.callInstance.getNetDataA().await()
                    aResult
                }

                val bResult = bDeferred.await().data
                val aResult = aDeferred.await().data

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
                val bDeferred = async {
                    val bResult = ApiSource.callInstance.getNetDataB().execute()
                    if (bResult.isSuccessful) {
                        bResult.body()!!
                    } else {
                        throw Throwable("b request failure")
                    }
                }

                val aDeferred = async {
                    val aResult = ApiSource.callInstance.getNetDataA().execute()
                    if (aResult.isSuccessful) {
                        aResult.body()!!
                    } else {
                        throw Throwable("a request failure")
                    }
                }

                val bResult = bDeferred.await().data
                val aResult = aDeferred.await().data

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
                val bDeferred = ApiSource.deferredInstance.getNetDataB()

                val aDeferred = ApiSource.deferredInstance.getNetDataA()

                val bResult = bDeferred.await().data

                val aResult = aDeferred.await().data

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