package com.zhxh.coroutines.guide

import kotlinx.coroutines.*

/**
Created by zhxh on 2019/05/27

协程上下文与调度器
协程总是运行在一些以 CoroutineContext 类型为代表的上下文中，它们被定义在了 Kotlin 的标准库里。
协程上下文是各种不同元素的集合。其中主元素是协程中的 Job， 我们在前面的文档中见过它以及它的调度器，而本文将对它进行介绍。



 */
object Test005 {

    @JvmStatic
    fun main(args: Array<String>) {
        testContext1()
        print("*******************************\n")
        testContext2()
        print("*******************************\n")
        testContext3()
    }


    /*
    我们使用普通的顺序来进行调用，因为这些代码是运行在协程中的，只要像常规的代码一样 顺序 都是默认的。
    下面的示例展示了测量执行两个挂起函数所需要的总时间：
     */
    fun testContext1() = runBlocking<Unit> {
        launch {
            // 运行在父协程的上下文中，即 runBlocking 主协程
            println("main runBlocking      : I'm working in thread ${Thread.currentThread().name}")
        }
        launch(Dispatchers.Unconfined) {
            // 不受限的——将工作在主线程中
            println("Unconfined            : I'm working in thread ${Thread.currentThread().name}")
        }
        launch(Dispatchers.Default) {
            // 将会获取默认调度器
            println("Default               : I'm working in thread ${Thread.currentThread().name}")
        }
        launch(newSingleThreadContext("MyOwnThread")) {
            // 将使它获得一个新的线程
            println("newSingleThreadContext: I'm working in thread ${Thread.currentThread().name}")
        }
    }

    /*
   非受限调度器 vs 受限调度器
   协程调度器默认承袭外部的 CoroutineScope 的调度器。
    特别是 runBlocking 的默认协程调度器仅限于调用者线程，因此承袭它将会把执行限制在该线程中， 并具有可预测的 FIFO 调度的效果。
     */

    fun testContext2() = runBlocking<Unit> {
        launch(Dispatchers.Unconfined) {
            // 非受限的——将和主线程一起工作
            println("Unconfined      : I'm working in thread ${Thread.currentThread().name}")
            delay(500)
            println("Unconfined      : After delay in thread ${Thread.currentThread().name}")
        }
        launch {
            // 父协程的上下文，主 runBlocking 协程
            println("main runBlocking: I'm working in thread ${Thread.currentThread().name}")
            delay(1000)
            println("main runBlocking: After delay in thread ${Thread.currentThread().name}")
        }
    }

    /*
   调试协程与线程
   协程调度器默认承袭外部的 CoroutineScope 的调度器。
    特别是 runBlocking 的默认协程调度器仅限于调用者线程，因此承袭它将会把执行限制在该线程中， 并具有可预测的 FIFO 调度的效果。
     */

    fun testContext3() = runBlocking<Unit> {
        val a = async {
            log("I'm computing a piece of the answer")
            8
        }
        val b = async {
            log("I'm computing another piece of the answer")
            7
        }
        log("The answer is ${a.await() * b.await()}")
    }

    fun log(msg: String) = println("[${Thread.currentThread().name}] $msg")

    /*
   在不同线程间跳转
    它演示了一些新技术。其中一个使用 runBlocking 来显式指定了一个上下文，并且另一个使用 withContext 函数来改变协程的上下文，
    而仍然驻留在相同的协程中，你可以在下面的输出中看到：
     */

    fun testContext4() {
        newSingleThreadContext("Ctx1").use { ctx1 ->
            newSingleThreadContext("Ctx2").use { ctx2 ->
                runBlocking(ctx1) {
                    log("Started in ctx1")
                    withContext(ctx2) {
                        log("Working in ctx2")
                    }
                    log("Back to ctx1")
                }
            }
        }
    }

    /*
   子协程
    当一个协程被其它协程在 CoroutineScope 中启动的时候，
     它将通过 CoroutineScope.coroutineContext 来承袭上下文，
     并且这个新协程的 Job 将会成为父协程任务的 子 任务。
     当一个父协程被取消的时候，所有它的子协程也会被递归的取消。
     */

    fun testContext5() = runBlocking<Unit> {
        // 启动一个协程来处理某种传入请求（request）
        val request = launch {
            // 孵化了两个子任务, 其中一个通过 GlobalScope 启动
            GlobalScope.launch {
                println("job1: I run in GlobalScope and execute independently!")
                delay(1000)
                println("job1: I am not affected by cancellation of the request")
            }
            // 另一个则承袭了父协程的上下文
            launch {
                delay(100)
                println("job2: I am a child of the request coroutine")
                delay(1000)
                println("job2: I will not execute this line if my parent request is cancelled")
            }
        }
        delay(500)
        request.cancel() // 取消请求（request）的执行
        delay(1000) // 延迟一秒钟来看看发生了什么
        println("main: Who has survived request cancellation?")
    }
    /*
   父协程的职责
   一个父协程总是等待所有的子协程执行结束。父协程并不显式的跟踪所有子协程的启动以及不必使用 Job.join 在最后的时候等待它们：，
   text 来承袭上下文，
   */

    fun testContext6() = runBlocking<Unit> {
        // 启动一个协程来处理某种传入请求（request）
        val request = launch {
            repeat(3) { i ->
                // 启动少量的子任务
                launch {
                    delay((i + 1) * 200L) // 延迟 200 毫秒、400 毫秒、600 毫秒的时间
                    println("Coroutine $i is done")
                }
            }
            println("request: I'm done and I don't explicitly join my children that are still active")
        }
        request.join() // 等待请求的完成，包括其所有子协程
        println("Now processing of the request is complete")
    }

    /*
    命名协程以用于调试
    协程日志会频繁记录的时候以及当你只是需要来自相同协程的关联日志记录，
    自动分配 id 是非常棒的。然而，当协程与执行一个明确的请求或与执行一些显式的后台任务有关的时候，出于调试的目的给它明确的命名是更好的做法。
    CoroutineName 上下文元素可以给线程像给函数命名一样命名。它在协程被执行且调试模式被开启时将显示线程的名字。
    */

    fun testContext7() = runBlocking<Unit> {
        log("Started main coroutine")
        // 运行两个后台值计算
        val v1 = async(CoroutineName("v1coroutine")) {
            delay(500)
            log("Computing v1")
            252
        }
        val v2 = async(CoroutineName("v2coroutine")) {
            delay(1000)
            log("Computing v2")
            6
        }
        log("The answer for v1 / v2 = ${v1.await() / v2.await()}")
    }


    /*
    ********************************************
    * 你可以看到，只有前两个协程打印了消息，而另一个协程在 Activity.destroy() 中被单次调用了 job.cancel()。
     */
    class Activity : CoroutineScope by CoroutineScope(Dispatchers.Default) {

        fun destroy() {
            cancel() // Extension on CoroutineScope
        }
        // 继续运行……

        // class Activity continues
        fun doSomething() {
            // 在示例中启动了 10 个协程，且每个都工作了不同的时长
            repeat(10) { i ->
                launch {
                    delay((i + 1) * 200L) // 延迟 200 毫秒、400 毫秒、600 毫秒等等不同的时间
                    println("Coroutine $i is done")
                }
            }
        }
    } // Activity 类结束

    fun testContext8() = runBlocking<Unit> {
        val activity = Activity()
        activity.doSomething() // 运行测试函数
        println("Launched coroutines")
        delay(500L) // 延迟半秒钟
        println("Destroying activity!")
        activity.destroy() // 取消所有的协程
        delay(1000) // 为了在视觉上确认它们没有工作
    }



    /*
    ********************************************
    * 线程局部数据
    * 在这个例子中我们使用 Dispatchers.Default 在后台线程池中启动了一个新的协程，所以它工作在线程池中的不同线程中，
    * 但它仍然具有线程局部变量的值，
     * 我们指定使用 threadLocal.asContextElement(value = "launch")，
     * 无论协程执行在什么线程中都是没有问题的。
     */

    val threadLocal = ThreadLocal<String?>() // 声明线程局部变量

    fun testContext9() = runBlocking<Unit> {
        threadLocal.set("main")
        println("Pre-main, current thread: ${Thread.currentThread()}, thread local value: '${threadLocal.get()}'")
        val job = launch(Dispatchers.Default + threadLocal.asContextElement(value = "launch")) {
            println("Launch start, current thread: ${Thread.currentThread()}, thread local value: '${threadLocal.get()}'")
            yield()
            println("After yield, current thread: ${Thread.currentThread()}, thread local value: '${threadLocal.get()}'")
        }
        job.join()
        println("Post-main, current thread: ${Thread.currentThread()}, thread local value: '${threadLocal.get()}'")
    }
}