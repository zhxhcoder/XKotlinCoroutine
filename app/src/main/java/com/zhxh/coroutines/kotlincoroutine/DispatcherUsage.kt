package com.zhxh.coroutines.kotlincoroutine

import android.os.Handler
import android.os.HandlerThread
import kotlinx.coroutines.android.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

object DispatcherUsage: ITestCase {

    private suspend fun doSomeWork() {
        printFormatMsg("inner start")
        Thread.sleep(200)
        printFormatMsg("inner end")
    }

    override fun test() {
        val handlerThread = HandlerThread("DispatcherUsage-thread")
        handlerThread.start()
        val myDispatcher = Handler(handlerThread.looper).asCoroutineDispatcher("My-Dispatcher")
        runBlocking(myDispatcher) {

            launch {
                doSomeWork()
            }

            run {
                printFormatMsg("outer start")
                Thread.sleep(200)
                printFormatMsg("outer end")
            }

            handlerThread.quitSafely()
        }

    }
}