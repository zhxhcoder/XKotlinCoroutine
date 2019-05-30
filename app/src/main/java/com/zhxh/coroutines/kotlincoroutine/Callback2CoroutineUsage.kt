package com.zhxh.coroutines.kotlincoroutine

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.RuntimeException
import kotlin.concurrent.thread
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.random.Random

object Callback2CoroutineUsage: ITestCase {

    private fun performAsyncTask(param: Int, callback: (Int, Boolean) -> Unit) {
        printFormatMsg("performAsyncTask() start")
        thread {
            // simulating the time consuming
            Thread.sleep(2000)
            callback.invoke(param + 2, Random.nextInt(2) == 0)
        }
        printFormatMsg("performAsyncTask() end")
    }

    private suspend fun coroutineWrapper(param: Int) = suspendCoroutine<Int> { continuation ->
        performAsyncTask(param) { result, success ->
            if (success) {
                continuation.resume(result)
            } else {
                continuation.resumeWith(Result.failure(RuntimeException("failed")))
            }
        }
    }

    override fun test() {
        GlobalScope.launch {
            try {
                printFormatMsg("coroutineWrapper result is ${coroutineWrapper(2)}")
            } catch (e: Exception) {
                printFormatMsg("some exception occurred, e=$e")
            }
        }
    }
}