package com.zhxh.coroutines.kotlincoroutine

import kotlinx.coroutines.*
import java.lang.RuntimeException

object ExceptionHandleUsage: ITestCase {

    private suspend fun exceptionWithinLaunch() = GlobalScope.launch {
        throw RuntimeException("Exception in launch")
    }

    private fun exceptionWithinAsync() = GlobalScope.async {
        throw RuntimeException("Exception in async")
    }

    private fun scopedExceptionHandle(): Deferred<*> {
        val scope = CoroutineScope(Job())
        return scope.async {
            printFormatMsg("launch parent coroutine")
            async {
                printFormatMsg("launch child coroutine")
                async {
                    printFormatMsg("launch grand child coroutine")
                    throw RuntimeException("Exception in grand child")
                }
            }
        }
    }

    private suspend fun execAndCatch(blocking: suspend () -> Unit) {
        try {
            blocking.invoke()
        } catch (t: Throwable) {
            printFormatMsg("Exception encountered ${t.message}")
        }
    }

    override fun test() {
        runBlocking {
//            execAndCatch { exceptionWithinLaunch() }  // can not catch it here, exception is just thrown where it happen
            val deferred = exceptionWithinAsync()
            execAndCatch { deferred.await() }
            val deferred2 = scopedExceptionHandle()
            execAndCatch { deferred2.await() }
        }
    }
}