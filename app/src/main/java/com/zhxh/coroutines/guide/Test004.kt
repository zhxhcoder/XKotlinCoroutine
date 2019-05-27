package com.zhxh.coroutines.guide

import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

/**
 * Created by zhxh on 2019/05/27
 *
 * 组合挂起函数
 */
object Test004 {

    @JvmStatic
    fun main(args: Array<String>) {
        testSuspend3()
    }


    /*
    我们使用普通的顺序来进行调用，因为这些代码是运行在协程中的，只要像常规的代码一样 顺序 都是默认的。
    下面的示例展示了测量执行两个挂起函数所需要的总时间：
     */
    fun testSuspend1() = runBlocking<Unit> {
        val time = measureTimeMillis {
            val one = doSomethingUsefulOne()
            val two = doSomethingUsefulTwo()
            println("The answer is ${one + two}")
        }
        println("Completed in $time ms")
    }

    suspend fun doSomethingUsefulOne(): Int {
        delay(1000L) // 假设我们在这里做了些有用的事
        return 16
    }

    suspend fun doSomethingUsefulTwo(): Int {
        delay(2000L) // 假设我们在这里也做了一些有用的事
        return 35
    }

    /*
    使用 async 并发
    如果 doSomethingUsefulOne 与 doSomethingUsefulTwo 之间没有依赖，并且我们想更快的得到结果，让它们进行 并发 吗？这就是 async 可以帮助我们的地方。
     */

    fun testSuspend2() = runBlocking<Unit> {
        val time = measureTimeMillis {
            val one = async { doSomethingUsefulOne() }
            val two = async { doSomethingUsefulTwo() }
            println("The answer is ${one.await() + two.await()}")
        }
        println("Completed in $time ms")
    }


    /*
    惰性启动的 async
    使用一个可选的参数 start 并传值 CoroutineStart.LAZY，
    可以对 async 进行惰性操作。 只有当结果需要被 await 或者如果一个 start 函数被调用，协程才会被启动。
     */

    fun testSuspend3() = runBlocking<Unit> {
        val time = measureTimeMillis {
            val one = async(start = CoroutineStart.LAZY) { doSomethingUsefulOne() }
            val two = async(start = CoroutineStart.LAZY) { doSomethingUsefulTwo() }
            // 执行一些计算
            one.start() // 启动第一个
            two.start() // 启动第二个
            println("The answer is ${one.await() + two.await()}")
        }
        println("Completed in $time ms")
    }

    /*
    async 风格的函数
    我们可以定义异步风格的函数来 异步 的调用 doSomethingUsefulOne 和 doSomethingUsefulTwo
     */
    // 注意，在这个示例中我们在 `main` 函数的右边没有加上 `runBlocking`
    fun testSuspend4() {
        val time = measureTimeMillis {
            // 我们可以在协程外面启动异步执行
            val one = somethingUsefulOneAsync()
            val two = somethingUsefulTwoAsync()
            // 但是等待结果必须调用其它的挂起或者阻塞
            // 当我们等待结果的时候，这里我们使用 `runBlocking { …… }` 来阻塞主线程
            runBlocking {
                println("The answer is ${one.await() + two.await()}")
            }
        }
        println("Completed in $time ms")
    }

    // somethingUsefulOneAsync 函数的返回值类型是 Deferred<Int>
    fun somethingUsefulOneAsync() = GlobalScope.async {
        doSomethingUsefulOne()
    }

    // somethingUsefulTwoAsync 函数的返回值类型是 Deferred<Int>
    fun somethingUsefulTwoAsync() = GlobalScope.async {
        doSomethingUsefulTwo()
    }

    /*
    使用 async 的结构化并发
     */
    fun testSuspend5() = runBlocking<Unit> {
        val time = measureTimeMillis {
            println("The answer is ${concurrentSum()}")
        }
        println("Completed in $time ms")
    }

    suspend fun concurrentSum(): Int = coroutineScope {
        val one = async { doSomethingUsefulOne() }
        val two = async { doSomethingUsefulTwo() }
        one.await() + two.await()
    }
    /*
    使用 async 的结构化并发
    取消始终通过协程的层次结构来进行传递：
    */

    fun testSuspend6() = runBlocking<Unit> {
        try {
            failedConcurrentSum()
        } catch (e: ArithmeticException) {
            println("Computation failed with ArithmeticException")
        }
    }

    suspend fun failedConcurrentSum(): Int = coroutineScope {
        val one = async<Int> {
            try {
                delay(Long.MAX_VALUE) // 模拟一个长时间的运算
                51
            } finally {
                println("First child was cancelled")
            }
        }
        val two = async<Int> {
            println("Second child throws an exception")
            throw ArithmeticException()
        }
        one.await() + two.await()
    }
}