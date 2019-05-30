package com.zhxh.coroutines.kotlincoroutine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CoroutineProducer(private val channel: SendChannel<Int>, val tname: String) {

    suspend fun run() {
        printFormatMsg("$tname started")
        var resource = 1
        while (true) {
            try {
                delay(10L)
                val nextValue = resource++
                printFormatMsg("$tname -> $nextValue")
                channel.send(nextValue)
                if (nextValue == 10) {
                    // an extra 100 to let the second consumer quit
                    channel.close()
                    break
                }
            } catch(e: InterruptedException) {
                printFormatMsg("interrupted")
            }
        }
        printFormatMsg("$tname quit...")
    }

}

class CoroutineConsumer(private val channel: ReceiveChannel<Int>, val tname: String) {

    suspend fun run() {
        printFormatMsg("$tname start")
        try {
            for (item in channel) {
                delay(50L)
                printFormatMsg("$tname => $item")
            }
        } catch (e: InterruptedException) {
            printFormatMsg("interrupted")
        }
        printFormatMsg("$tname quit...")
    }

}

object CoroutineProducerConsumer: ITestCase {

    private lateinit var channel: Channel<Int>
    private lateinit var producer: CoroutineProducer
    private lateinit var consumer1: CoroutineConsumer
    private lateinit var consumer2: CoroutineConsumer

    override fun test() {
        channel = Channel<Int>(10)
        producer = CoroutineProducer(channel, "producer")
        consumer1 = CoroutineConsumer(channel, "consumer1")
        consumer2 = CoroutineConsumer(channel, "consumer2")
        GlobalScope.launch(Dispatchers.Default) {
            printFormatMsg("======> CoroutineProducerConsumer begin")
            val before = System.currentTimeMillis()
            val job1 = GlobalScope.launch(Dispatchers.Default) { producer.run() }
            val job2 = GlobalScope.launch(Dispatchers.Default) { consumer1.run() }
            val job3 = GlobalScope.launch(Dispatchers.Default) { consumer2.run() }
            job1.join()
            job2.join()
            job3.join()
            printFormatMsg("CoroutineProducerConsumer time = ${System.currentTimeMillis() - before}")
        }
    }
}