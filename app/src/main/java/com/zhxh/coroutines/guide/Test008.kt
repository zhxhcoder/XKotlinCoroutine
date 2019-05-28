package com.zhxh.coroutines.guide

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.measureTimeMillis

/**
 * Created by zhxh on 2019/05/28
 *
 * 共享的可变状态与并发
 * 协程可用多线程调度器（比如默认的 Dispatchers.Default）并发执行。这样就可以提出所有常见的并发问题。
 * 主要的问题是同步访问共享的可变状态。
 * 协程领域对这个问题的一些解决方案类似于多线程领域中的解决方案， 但其它解决方案则是独一无二的。
 */
object Test008 {

    @JvmStatic
    fun main(args: Array<String>) {
        testConcurrent1()
        testConcurrent2()
        testConcurrent3()
        testConcurrent4()
        testConcurrent5()
        testConcurrent6()
    }

    suspend fun CoroutineScope.massiveRun(action: suspend () -> Unit) {
        val n = 100  // 启动的协程数量
        val k = 1000 // 每个协程重复执行同一动作的次数
        val time = measureTimeMillis {
            val jobs = List(n) {
                launch {
                    repeat(k) { action() }
                }
            }
            jobs.forEach { it.join() }
        }
        println("Completed ${n * k} actions in $time ms")
    }

    @Volatile // 在 Kotlin 中 `volatile` 是一个注解
    var counter1 = 0

    /*
    ********************************************
    volatile 也无济于事：这段代码运行速度更慢了，但我们最后仍然没有得到“Counter = 100000”这个结果，
    因为 volatile 变量保证可线性化（这是“原子”的技术术语）读取和写入变量，
    但在大量动作（在我们的示例中即“递增”操作）发生时并不提供原子性。
    */
    private fun testConcurrent1() = runBlocking<Unit> {
        GlobalScope.massiveRun {
            counter1++
        }
        println("Counter = $counter1")
    }


    /*
    ********************************************
    这是针对此类特定问题的最快解决方案。
    它适用于普通计数器、集合、队列和其他标准数据结构以及它们的基本操作。
    然而，它并不容易被扩展来应对复杂状态、或一些没有现成的线程安全实现的复杂操作。
     */
    var counter2 = AtomicInteger()

    fun testConcurrent2() = runBlocking<Unit> {
        GlobalScope.massiveRun {
            counter2.incrementAndGet()
        }
        println("Counter = ${counter2.get()}")
    }

    /*
    ********************************************
    以细粒度限制线程
    限制线程 是解决共享可变状态问题的一种方案：对特定共享状态的所有访问权都限制在单个线程中。
    它通常应用于 UI 程序中：所有 UI 状态都局限于单个事件分发线程或应用主线程中。
    这在协程中很容易实现，通过使用一个单线程上下文：

    这段代码运行非常缓慢，因为它进行了 细粒度 的线程限制。每个增量操作都得使用 withContext 块从多线程 Dispatchers.Default 上下文切换到单线程上下文。
    */
    private val counterContext3 = newSingleThreadContext("CounterContext")
    var counter3 = 0

    fun testConcurrent3() = runBlocking<Unit> {
        GlobalScope.massiveRun {
            // 使用 DefaultDispathcer 运行每个协程
            withContext(counterContext3) {
                // 但是把每个递增操作都限制在此单线程上下文中
                counter3++
            }
        }
        println("Counter = $counter3")
    }

    /*
    ********************************************
    以粗粒度限制线程
    在实践中，线程限制是在大段代码中执行的，例如：状态更新类业务逻辑中大部分都是限于单线程中。
    下面的示例演示了这种情况， 在单线程上下文中运行每个协程。
    这里我们使用 CoroutineScope() 函数来切换协程上下文为 CoroutineScope：
    */
    val counterContext4 = newSingleThreadContext("CounterContext")
    var counter4 = 0

    fun testConcurrent4() = runBlocking<Unit> {
        CoroutineScope(counterContext4).massiveRun {
            // 在单线程上下文中运行每个协程
            counter4++
        }
        println("Counter = $counter4")
    }

    /*
    ********************************************
    互斥
    该问题的互斥解决方案：使用永远不会同时执行的 关键代码块 来保护共享状态的所有修改。
    在阻塞的世界中，你通常会为此目的使用 synchronized 或者 ReentrantLock。
    在协程中的替代品叫做 Mutex 。它具有 lock 和 unlock 方法， 可以隔离关键的部分。
    关键的区别在于 Mutex.lock() 是一个挂起函数，它不会阻塞线程。
    */
    val mutex5 = Mutex()
    var counter5 = 0

    fun testConcurrent5() = runBlocking<Unit> {
        GlobalScope.massiveRun {
            mutex5.withLock {
                counter5++
            }
        }
        println("Counter = $counter5")
    }


    /*
    ********************************************
    Actors
    一个 actor 是由协程、被限制并封装到该协程中的状态以及一个与其它协程通信的 通道 组合而成的一个实体。
    一个简单的 actor 可以简单的写成一个函数， 但是一个拥有复杂状态的 actor 更适合由类来表示。

    有一个 actor 协程构建器，它可以方便地将 actor 的邮箱通道组合到其作用域中（用来接收消息）、组合发送 channel 与结果集对象，
    这样对 actor 的单个引用就可以作为其句柄持有。

    使用 actor 的第一步是定义一个 actor 要处理的消息类。
    Kotlin 的密封类很适合这种场景。 我们使用 IncCounter 消息（用来递增计数器）和 GetCounter 消息（用来获取值）来定义 CounterMsg 密封类。
    后者需要发送回复。CompletableDeferred 通信原语表示未来可知（可传达）的单个值， 这里被用于此目的。
    */


    // 这个函数启动一个新的计数器 actor
    fun CoroutineScope.counterActor() = actor<CounterMsg> {
        var counter = 0 // actor 状态
        for (msg in channel) { // 即将到来消息的迭代器
            when (msg) {
                is IncCounter -> counter++
                is GetCounter -> msg.response.complete(counter)
            }
        }
    }

    fun testConcurrent6() = runBlocking<Unit> {
        val counter = counterActor() // 创建该 actor
        GlobalScope.massiveRun {
            counter.send(IncCounter)
        }
        // 发送一条消息以用来从一个 actor 中获取计数值
        val response = CompletableDeferred<Int>()
        counter.send(GetCounter(response))
        println("Counter = ${response.await()}")
        counter.close() // 关闭该actor
    }
}
// 计数器 Actor 的各种类型
sealed class CounterMsg
object IncCounter : CounterMsg() // 递增计数器的单向消息
class GetCounter(val response: CompletableDeferred<Int>) : CounterMsg() // 携带回复的请求