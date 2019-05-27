package com.zhxh.coroutines.guide

import kotlinx.coroutines.*

/**
 * Created by zhxh on 2019/05/27
 *
 * 异常处理
 */
object Test006 {

    @JvmStatic
    fun main(args: Array<String>) {

    }

    /*
    ********************************************
    * 协程构建器有两种风格：自动的传播异常（launch 以及 actor） 或者将它们暴露给用户（async 以及 produce）。
    * 前者对待异常是不处理的，类似于 Java 的 Thread.uncaughtExceptionHandler， 而后者依赖用户来最终消耗异常，
    * 比如说，通过 await 或 receive （produce 以及 receive 在通道中介绍过）。
     */
    fun testException1() = runBlocking {
        val job = GlobalScope.launch {
            println("Throwing exception from launch")
            throw IndexOutOfBoundsException() // 我们将在控制台打印 Thread.defaultUncaughtExceptionHandler
        }
        job.join()
        println("Joined failed job")
        val deferred = GlobalScope.async {
            println("Throwing exception from async")
            throw ArithmeticException() // 没有打印任何东西，依赖用户去调用等待
        }
        try {
            deferred.await()
            println("Unreached")
        } catch (e: ArithmeticException) {
            println("Caught ArithmeticException")
        }
    }

    /*
    ********************************************
    * CoroutineExceptionHandler 仅在预计不会由用户处理的异常上调用， 所以在 async 构建器中注册它没有任何效果。duce）。
    * 依赖用户来最终消耗异常，
     */
    fun testException2() = runBlocking {
        val handler = CoroutineExceptionHandler { _, exception ->
            println("Caught $exception")
        }
        val job = GlobalScope.launch(handler) {
            throw AssertionError()
        }
        val deferred = GlobalScope.async(handler) {
            throw ArithmeticException() // 没有打印任何东西，依赖用户去调用 deferred.await()
        }
        joinAll(job, deferred)
    }

    /*
    ********************************************
    * 取消与异常
    * 取消与异常紧密相关。协程内部使用 CancellationException 来进行取消，
    * 这个异常会被所有的处理者忽略，所以那些可以被 catch 代码块捕获的异常仅仅应该被用来作为额外调试信息的资源。
     * 当一个协程在没有任何理由的情况下使用 Job.cancel 取消的时候，它会被终止，但是它不会取消它的父协程。
      * 无理由取消是父协程取消其子协程而非取消其自身的机制。
     */
    fun testException3() = runBlocking {
        val job = launch {
            val child = launch {
                try {
                    delay(Long.MAX_VALUE)
                } finally {
                    println("Child is cancelled")
                }
            }
            yield()
            println("Cancelling child")
            child.cancel()
            child.join()
            yield()
            println("Parent is not cancelled")
        }
        job.join()
    }


    /*
    ********************************************
    * 如果协程遇到除 CancellationException 以外的异常，它将取消具有该异常的父协程。
     * 这种行为不能被覆盖，且它被用来提供一个稳定的协程层次结构来进行结构化并发而无需依赖 CoroutineExceptionHandler 的实现。
      * 且当所有的子协程被终止的时候，原本的异常被父协程所处理。
     */

    fun testException4() = runBlocking {
        val handler = CoroutineExceptionHandler { _, exception ->
            println("Caught $exception")
        }
        val job = GlobalScope.launch(handler) {
            launch {
                // 第一个子协程
                try {
                    delay(Long.MAX_VALUE)
                } finally {
                    withContext(NonCancellable) {
                        println("Children are cancelled, but exception is not handled until all children terminate")
                        delay(100)
                        println("The first child finished its non cancellable block")
                    }
                }
            }
            launch {
                // 第二个子协程
                delay(10)
                println("Second child throws an exception")
                throw ArithmeticException()
            }
        }
        job.join()
    }
}