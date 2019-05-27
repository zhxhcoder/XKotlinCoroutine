package com.zhxh.coroutines.guide

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*

/**
 * Created by zhxh on 2019/05/27
 *
 * 一个 Channel 是一个和 BlockingQueue 非常相似的概念。
 * 其中一个不同是它代替了阻塞的 put 操作并提供了挂起的 send，
 * 还替代了阻塞的 take 操作并提供了挂起的 receive。
 */
object Test003 {

    @JvmStatic
    fun main(args: Array<String>) {
        testChannel5()
    }

    fun testChannel1() = runBlocking {
        val channel = Channel<Int>()
        launch {
            // 这里可能是消耗大量 CPU 运算的异步逻辑，我们将仅仅做 5 次整数的平方并发送
            for (x in 1..5) channel.send(x * x)
        }
        // 这里我们打印了 5 次被接收的整数：
        repeat(5) { println(channel.receive()) }
        println("Done!")
    }

    /*
     *
     */
    fun testChannel2() = runBlocking {
        val channel = Channel<Int>()
        launch {
            for (x in 1..5) channel.send(x * x)
            channel.close() // 我们结束发送
        }
        // 这里我们使用 `for` 循环来打印所有被接收到的元素（直到通道被关闭）
        for (y in channel) println(y)
        println("Done!")
    }

    /*
     * 这里有一个名为 produce 的便捷的协程构建器，可以很容易的在生产者端正确工作，
     * 并且我们使用扩展函数 consumeEach 在消费者端替代 for 循环：
     */
    fun CoroutineScope.produceSquares(): ReceiveChannel<Int> = produce {
        for (x in 1..5) send(x * x)
    }

    fun testChannel3() = runBlocking {
        val squares = produceSquares()
        squares.consumeEach { println(it) }
        println("Done!")
    }

    /*
     *
     */
    fun testChannel4() = runBlocking {
        val numbers = produceNumbers() // 从 1 开始生产整数
        val squares = square(numbers) // 对整数做平方
        for (i in 1..5) println(squares.receive()) // 打印前 5 个数字
        println("Done!") // 我们的操作已经结束了
        coroutineContext.cancelChildren() // 取消子协程
    }

    /*
     * 管道是一种一个协程在流中开始生产可能无穷多个元素的模式：
     */
    fun CoroutineScope.produceNumbers() = produce<Int> {
        var x = 1
        while (true) send(x++) // 从 1 开始的无限的整数流
    }

    /*
     * 并且另一个或多个协程开始消费这些流，做一些操作，
     * 并生产了一些额外的结果。 在下面的例子中，对这些数字仅仅做了平方操作：
     */
    fun CoroutineScope.square(numbers: ReceiveChannel<Int>): ReceiveChannel<Int> = produce {
        for (x in numbers) send(x * x)
    }


    /*
    多个协程也许会接收相同的管道，在它们之间进行分布式工作。 让我们启动一个定期产生整数的生产者协程 （每秒十个数字）：
    */
    fun testChannel5() = runBlocking<Unit> {
        val producer = produceNumbers5()
        repeat(5) { launchProcessor(it, producer) }
        delay(950)
        producer.cancel() // cancel producer coroutine and thus kill them all
    }

    fun CoroutineScope.produceNumbers5() = produce<Int> {
        var x = 1 // start from 1
        while (true) {
            send(x++) // produce next
            delay(100) // wait 0.1s
        }
    }

    fun CoroutineScope.launchProcessor(id: Int, channel: ReceiveChannel<Int>) = launch {
        for (msg in channel) {
            println("Processor #$id received $msg")
        }
    }

    /*
多个协程可以发送到同一个通道。 比如说，让我们创建一个字符串的通道，和一个在这个通道中以指定的延迟反复发送一个指定字符串的挂起函数：
    */
    fun testChannel6() = runBlocking {
        val channel = Channel<String>()
        launch { sendString(channel, "foo", 200L) }
        launch { sendString(channel, "BAR!", 500L) }
        repeat(6) {
            // 接收前六个
            println(channel.receive())
        }
        coroutineContext.cancelChildren() // 取消所有子协程来让主协程结束
    }

    /*
     *多个协程可以发送到同一个通道。 比如说，让我们创建一个字符串的通道，和一个在这个通道中以指定的延迟反复发送一个指定字符串的挂起函数：
    */
    suspend fun sendString(channel: SendChannel<String>, s: String, time: Long) {
        while (true) {
            delay(time)
            channel.send(s)
        }
    }

    /*
Channel() 工厂函数与 produce 建造器通过一个可选的参数 capacity 来指定 缓冲区大小 。
缓冲允许发送者在被挂起前发送多个元素，
就像 BlockingQueue 有指定的容量一样，当缓冲区被占满的时候将会引起阻塞。
    */
    fun testChannel7() = runBlocking<Unit> {
        val channel = Channel<Int>(4) // 启动带缓冲的通道
        val sender = launch {
            // 启动发送者协程
            repeat(10) {
                println("Sending $it") // 在每一个元素发送前打印它们
                channel.send(it) // 将在缓冲区被占满时挂起
            }
        }
        // 没有接收到东西……只是等待……
        delay(1000)
        sender.cancel() // 取消发送者协程
    }


    /*
发送和接收操作是 公平的 并且尊重调用它们的多个协程。
它们遵守先进先出原则，可以看到第一个协程调用 receive 并得到了元素。
在下面的例子中两个协程“乒”和“乓”都从共享的“桌子”通道接收到这个“球”元素。
*/
    data class Ball(var hits: Int)

    fun testChannel8() = runBlocking {
        val table = Channel<Ball>() // 一个共享的 table（桌子）
        launch { player("ping", table) }
        launch { player("pong", table) }
        table.send(Ball(0)) // 乒乓球
        delay(1000) // 延迟 1 秒钟
        coroutineContext.cancelChildren() // 游戏结束，取消它们
    }

    suspend fun player(name: String, table: Channel<Ball>) {
        for (ball in table) { // 在循环中接收球
            ball.hits++
            println("$name $ball")
            delay(300) // 等待一段时间
            table.send(ball) // 将球发送回去
        }
    }

    /*
    计时器通道是一种特别的会合通道，每次经过特定的延迟都会从该通道进行消费并产生 Unit。 虽然它看起来似乎没用，它被用来构建分段来创建复杂的基于时间的 produce 管道和进行窗口化操作以及其它时间相关的处理。 可以在 select 中使用计时器通道来进行“打勾”操作。
     */
    fun testChannel9() = runBlocking<Unit> {
        val tickerChannel = ticker(delayMillis = 100, initialDelayMillis = 0) //创建计时器通道
        var nextElement = withTimeoutOrNull(1) { tickerChannel.receive() }
        println("Initial element is available immediately: $nextElement") // 初始尚未经过的延迟

        nextElement = withTimeoutOrNull(50) { tickerChannel.receive() } // 所有随后到来的元素都经过了 100 毫秒的延迟
        println("Next element is not ready in 50 ms: $nextElement")

        nextElement = withTimeoutOrNull(60) { tickerChannel.receive() }
        println("Next element is ready in 100 ms: $nextElement")

        // 模拟大量消费延迟
        println("Consumer pauses for 150ms")
        delay(150)
        // 下一个元素立即可用
        nextElement = withTimeoutOrNull(1) { tickerChannel.receive() }
        println("Next element is available immediately after large consumer delay: $nextElement")
        // 请注意，`receive` 调用之间的暂停被考虑在内，下一个元素的到达速度更快
        nextElement = withTimeoutOrNull(60) { tickerChannel.receive() }
        println("Next element is ready in 50ms after consumer pause in 150ms: $nextElement")

        tickerChannel.cancel() // 表明不再需要更多的元素
    }

}