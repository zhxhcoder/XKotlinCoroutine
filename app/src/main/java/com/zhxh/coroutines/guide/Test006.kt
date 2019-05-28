package com.zhxh.coroutines.guide

import kotlinx.coroutines.*
import java.io.IOException

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

    /*
********************************************
* 如果一个协程的多个子协程抛出异常将会发生什么？
* 通常的规则是“第一个异常赢得了胜利”，所以第一个被抛出的异常将会暴露给处理者。
 * 但也许这会是异常丢失的原因，比如说一个协程在 finally 块中抛出了一个异常。
 * 这时，多余的异常将会被压制。
 */

    fun testException5() = runBlocking {
        val handler = CoroutineExceptionHandler { _, exception ->
            println("Caught $exception with suppressed ${exception.suppressed.contentToString()}")
        }
        val job = GlobalScope.launch(handler) {
            launch {
                try {
                    delay(Long.MAX_VALUE)
                } finally {
                    throw ArithmeticException()
                }
            }
            launch {
                delay(100)
                throw IOException()
            }
            delay(Long.MAX_VALUE)
        }
        job.join()
    }

    /*
    ********************************************
    取消异常是透明的并且会在默认情况下解包：
    */
    fun testException6() = runBlocking {
        val handler = CoroutineExceptionHandler { _, exception ->
            println("Caught original $exception")
        }
        val job = GlobalScope.launch(handler) {
            val inner = launch {
                launch {
                    launch {
                        throw IOException()
                    }
                }
            }
            try {
                inner.join()
            } catch (e: CancellationException) {
                println("Rethrowing CancellationException with original cause")
                throw e
            }
        }
        job.join()
    }

    /*
    ********************************************
    监督任务：SupervisorJob 可以被用于这些目的。它类似于常规的 Job，唯一的取消异常将只会向下传播。
    */
    fun testException7() = runBlocking {
        val supervisor = SupervisorJob()
        with(CoroutineScope(coroutineContext + supervisor)) {
            // 启动第一个子任务——这个示例将会忽略它的异常（不要在实践中这么做！）
            val firstChild = launch(CoroutineExceptionHandler { _, _ -> }) {
                println("First child is failing")
                throw AssertionError("First child is cancelled")
            }
            // 启动第二个子任务
            val secondChild = launch {
                firstChild.join()
                // 取消了第一个子任务且没有传播给第二个子任务
                println("First child is cancelled: ${firstChild.isCancelled}, but second one is still active")
                try {
                    delay(Long.MAX_VALUE)
                } finally {
                    // 但是取消了监督的传播
                    println("Second child is cancelled because supervisor is cancelled")
                }
            }
            // 等待直到第一个子任务失败且执行完成
            firstChild.join()
            println("Cancelling supervisor")
            supervisor.cancel()
            secondChild.join()
        }
    }
    /*
    ********************************************
    监督作用域：对于作用域的并发，supervisorScope 可以被用来替代 coroutineScope 来实现相同的目的。
    它只会单向的传播并且当子任务自身执行失败的时候将它们全部取消。它也会在所有的子任务执行结束前等待，
     就像 coroutineScope 所做的那样。
    */

    fun testException8() = runBlocking {
        try {
            supervisorScope {
                val child = launch {
                    try {
                        println("Child is sleeping")
                        delay(Long.MAX_VALUE)
                    } finally {
                        println("Child is cancelled")
                    }
                }
                // 使用 yield 来给我们的子任务一个机会来执行打印
                yield()
                println("Throwing exception from scope")
                throw AssertionError()
            }
        } catch (e: AssertionError) {
            println("Caught assertion error")
        }
    }
    /*
    ********************************************
    监督协程中的异常：常规的任务和监督任务之间的另一个重要区别是异常处理。
    每一个子任务应该通过异常处理机制处理自身的异常。
    这种差异来自于子任务的执行失败不会传播给它的父任务的事实。
    */

    fun testException9() = runBlocking {
        val handler = CoroutineExceptionHandler { _, exception ->
            println("Caught $exception")
        }
        supervisorScope {
            val child = launch(handler) {
                println("Child throws an exception")
                throw AssertionError()
            }
            println("Scope is completing")
        }
        println("Scope is completed")
    }
}

