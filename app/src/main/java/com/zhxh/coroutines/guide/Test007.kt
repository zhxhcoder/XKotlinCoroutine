package com.zhxh.coroutines.guide

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.selects.select
import java.io.IOException
import kotlin.random.Random

/**
 * Created by zhxh on 2019/05/28
 *
 * select 表达式（实验性的）
 */
object Test007 {

    @JvmStatic
    fun main(args: Array<String>) {
        testSelect1()
    }

    /*
    ********************************************
    * 在通道中 select
    */
    fun CoroutineScope.fizz() = produce<String> {
        while (true) { // 每 300 毫秒发送一个 "Fizz"
            delay(300)
            send("Fizz")
        }
    }

    fun CoroutineScope.buzz() = produce<String> {
        while (true) { // 每 500 毫秒发送一个 "Buzz!"
            delay(500)
            send("Buzz!")
        }
    }

    suspend fun selectFizzBuzz(fizz: ReceiveChannel<String>, buzz: ReceiveChannel<String>) {
        select<Unit> {
            // <Unit> 意味着该 select 表达式不返回任何结果
            fizz.onReceive { value ->
                // 这是第一个 select 子句
                println("fizz -> '$value'")
            }
            buzz.onReceive { value ->
                // 这是第二个 select 子句
                println("buzz -> '$value'")
            }
        }
    }

    fun testSelect1() = runBlocking<Unit> {
        val fizz = fizz()
        val buzz = buzz()
        repeat(7) {
            selectFizzBuzz(fizz, buzz)
        }
        coroutineContext.cancelChildren() // 取消 fizz 和 buzz 协程
    }

    /*
    ********************************************
    * 通道关闭时 select
    */
    suspend fun selectAorB(a: ReceiveChannel<String>, b: ReceiveChannel<String>): String =
        select<String> {
            a.onReceiveOrNull { value ->
                if (value == null)
                    "Channel 'a' is closed"
                else
                    "a -> '$value'"
            }
            b.onReceiveOrNull { value ->
                if (value == null)
                    "Channel 'b' is closed"
                else
                    "b -> '$value'"
            }
        }

    fun testSelect2() = runBlocking<Unit> {
        val a = produce<String> {
            repeat(4) { send("Hello $it") }
        }
        val b = produce<String> {
            repeat(4) { send("World $it") }
        }
        repeat(8) {
            // 打印最早的八个结果
            println(selectAorB(a, b))
        }
        coroutineContext.cancelChildren()
    }

    /*
    ********************************************
    * Select 以发送
    * 我们来编写一个整数生成器的示例，当主通道上的消费者无法跟上它时，它会将值发送到 side 通道上：
    */
    fun CoroutineScope.produceNumbers(side: SendChannel<Int>) = produce<Int> {
        for (num in 1..10) { // 生产从 1 到 10 的 10 个数值
            delay(100) // 延迟 100 毫秒
            select<Unit> {
                onSend(num) {} // 发送到主通道
                side.onSend(num) {} // 或者发送到 side 通道
            }
        }
    }

    fun testSelect3() = runBlocking<Unit> {
        val side = Channel<Int>() // 分配 side 通道
        launch {
            // 对于 side 通道来说，这是一个很快的消费者
            side.consumeEach { println("Side channel has $it") }
        }
        produceNumbers(side).consumeEach {
            println("Consuming $it")
            delay(250) // 不要着急，让我们正确消化消耗被发送来的数字
        }
        println("Done consuming")
        coroutineContext.cancelChildren()
    }

    /*
    ********************************************
    * Select 延迟值
    * 延迟值可以使用 onAwait 子句查询。 让我们启动一个异步函数，它在随机的延迟后会延迟返回字符串：
    */
    fun CoroutineScope.asyncString(time: Int) = async {
        delay(time.toLong())
        "Waited for $time ms"
    }

    fun CoroutineScope.asyncStringsList(): List<Deferred<String>> {
        val random = Random(3)
        return List(12) { asyncString(random.nextInt(1000)) }
    }

    fun testSelect4() = runBlocking<Unit> {
        val list = asyncStringsList()
        val result = select<String> {
            list.withIndex().forEach { (index, deferred) ->
                deferred.onAwait { answer ->
                    "Deferred $index produced answer '$answer'"
                }
            }
        }
        println(result)
        val countActive = list.count { it.isActive }
        println("$countActive coroutines are still active")
    }

    /*
    ********************************************
    * 在延迟值通道上切换
    * 我们现在来编写一个通道生产者函数，它消费一个产生延迟字符串的通道，并等待每个接收的延迟值，
    * 但它只在下一个延迟值到达或者通道关闭之前处于运行状态。
    * 此示例将 onReceiveOrNull 和 onAwait 子句放在同一个 select 中：
    */
    fun CoroutineScope.switchMapDeferreds(input: ReceiveChannel<Deferred<String>>) = produce<String> {
        var current = input.receive() // 从第一个接收到的延迟值开始
        while (isActive) { // 循环直到被取消或关闭
            val next = select<Deferred<String>?> {
                // 从这个 select 中返回下一个延迟值或 null
                input.onReceiveOrNull { update ->
                    update // 替换下一个要等待的值
                }
                current.onAwait { value ->
                    send(value) // 发送当前延迟生成的值
                    input.receiveOrNull() // 然后使用从输入通道得到的下一个延迟值
                }
            }
            if (next == null) {
                println("Channel was closed")
                break // 跳出循环
            } else {
                current = next
            }
        }
    }

    fun CoroutineScope.asyncString(str: String, time: Long) = async {
        delay(time)
        str
    }

    fun testSelect5() = runBlocking<Unit> {
        val chan = Channel<Deferred<String>>() // 测试使用的通道
        launch {
            // 启动打印协程
            for (s in switchMapDeferreds(chan))
                println(s) // 打印每个获得的字符串
        }
        chan.send(asyncString("BEGIN", 100))
        delay(200) // 充足的时间来生产 "BEGIN"
        chan.send(asyncString("Slow", 500))
        delay(100) // 不充足的时间来生产 "Slow"
        chan.send(asyncString("Replace", 100))
        delay(500) // 在最后一个前给它一点时间
        chan.send(asyncString("END", 500))
        delay(1000) // 给执行一段时间
        chan.close() // 关闭通道……
        delay(500) // 然后等待一段时间来让它结束
    }

}

