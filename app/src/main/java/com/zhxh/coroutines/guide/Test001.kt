package com.zhxh.coroutines.guide

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.concurrent.thread

/**
 * Created by zhxh on 2019/05/27
 *
 * 协程基础
 */
object Test001 {

    @JvmStatic
    fun main(args: Array<String>) {
        //firstCoroutine()
        //firstJava()
        testRunBlocking()
        testJoin()
    }

    private fun firstCoroutine() {//第一个协程程序
        GlobalScope.launch {
            // 在后台启动一个新的协程并继续
            delay(2000L) // 非阻塞的等待 2 秒钟（默认时间单位是毫秒）-delay 是一个特殊的 挂起函数 ，它不会造成线程阻塞，但是会 挂起 协程，并且只能在协程中使用。

            println("K-World!") // 在延迟后打印输出
        }
        println("K-Hello,") // 协程已在等待时主线程还在继续
        Thread.sleep(5000L) // 阻塞主线程 5 秒钟来保证 JVM 存活
    }

    private fun firstJava() {
        thread {
            // 在后台启动一个新的线程并继续
            Thread.sleep(2000L) // 阻塞的等待 2 秒钟（默认时间单位是毫秒）
            println("J-World!") // 在延迟后打印输出
        }
        println("J-Hello,") // 协程已在等待时主线程还在继续
        Thread.sleep(5000L) // 阻塞主线程 5 秒钟来保证 JVM 存活
    }


    private fun testRunBlocking() {
        GlobalScope.launch {
            // 在后台启动一个新的协程并继续
            delay(2000L)
            println("World!")
        }
        println("Hello,") // 主线程中的代码会立即执行
        runBlocking {
            //调用了 runBlocking 的主线程会一直 阻塞 直到 runBlocking 内部的协程执行完毕。
            // 但是这个表达式阻塞了主线程
            delay(3000L)  // ……我们延迟 3 秒来保证 JVM 的存活
        }
    }

    fun testJoin() = runBlocking {
        val job = GlobalScope.launch {
            //启动一个新协程并保持对这个作业的引用
            delay(1000L)
            println("World!")
        }
        println("Hello,")
        job.join() // 等待直到子协程执行结束
    }

}