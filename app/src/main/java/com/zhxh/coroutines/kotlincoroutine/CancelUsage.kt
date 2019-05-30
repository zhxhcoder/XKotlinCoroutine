package com.zhxh.coroutines.kotlincoroutine

import kotlinx.coroutines.*

object CancelUsage: ITestCase {

    private suspend fun separatedScope() {
        printFormatMsg("----- separatedScope() -----")
        val outer = CoroutineScope(Job())
        val parentJob = outer.launch {
            val inner = CoroutineScope(Job())

            inner.launch {
                delay(10)
                printFormatMsg("separatedScope inner1 done")
            }.apply {
                invokeOnCompletion {
                    printFormatMsg("separatedScope inner Job done ${it?.javaClass?.simpleName}")
                }
            }

            inner.launch {
                delay(200)
                printFormatMsg("separatedScope inner2 done")
            }.apply {
                invokeOnCompletion {
                    printFormatMsg("separatedScope inner Job done ${it?.javaClass?.simpleName}")
                }
            }
        }.apply {
            invokeOnCompletion {
            printFormatMsg("separatedScope outer Job done ${it?.javaClass?.simpleName}")
        } }
        delay(50)
        printFormatMsg("separatedScope canceling outer job")
        parentJob.cancel()
    }

    private suspend fun parentScope() {
        printFormatMsg("----- parentScope() -----")
        val outer = CoroutineScope(Job())

        val parentJob = outer.launch {
            launch {
                delay(10)
                printFormatMsg("parentScope inner1 done")
            }

            launch {
                delay(200)
                printFormatMsg("parentScope inner2 done")
            }
        }.apply {
            invokeOnCompletion {
                printFormatMsg("parentScope outer Job done ${it?.javaClass?.simpleName}")
            }
        }
        delay(50)
        printFormatMsg("parentScope canceling outer job")
        parentJob.cancel()
    }


    override fun test() {
        runBlocking {
            separatedScope()
            delay(300)
            parentScope()
        }
    }
}