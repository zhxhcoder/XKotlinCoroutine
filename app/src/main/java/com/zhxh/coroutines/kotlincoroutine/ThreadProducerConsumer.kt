package com.zhxh.coroutines.kotlincoroutine

import java.util.concurrent.ArrayBlockingQueue
import kotlin.concurrent.thread

class ThreadProducer(private val queue: ArrayBlockingQueue<Int>, val tname: String) : Thread(tname) {

    private var started = false

    override fun start() {
        started = true
        super.start()
    }

    override fun run() {
        printFormatMsg("$tname started")
        var resource = 1
        while (started) {
            try {
                sleep(10L)
                val nextValue = resource++
                printFormatMsg("$tname -> $nextValue")
                queue.put(nextValue)
                if (nextValue == 10) {
                    // an extra 100 to let the second consumer quit
                    queue.put(nextValue)
                    started = false
                }
            } catch (e: InterruptedException) {
                printFormatMsg("interrupted")
            }
        }
        printFormatMsg("$tname quit...")
    }

}

class ThreadConsumer(private val queue: ArrayBlockingQueue<Int>, val tname: String) : Thread(tname) {

    private var started = false

    override fun start() {
        started = true
        super.start()
    }

    override fun run() {
        printFormatMsg("$tname start")
        while (started) {
            try {
                sleep(50L)
                val nextValue = queue.take()
                printFormatMsg("$tname => $nextValue")
                if (nextValue == 10) {
                    started = false
                }
            } catch (e: InterruptedException) {
                printFormatMsg("interrupted")
            }
        }
        printFormatMsg("$tname quit...")
    }

}

object ThreadProducerConsumer : ITestCase {

    private lateinit var producer: ThreadProducer
    private lateinit var consumer1: ThreadConsumer
    private lateinit var consumer2: ThreadConsumer
    private val queue: ArrayBlockingQueue<Int> = ArrayBlockingQueue(10)

    override fun test() {
        queue.clear()
        producer = ThreadProducer(queue, "producer")
        consumer1 = ThreadConsumer(queue, "consumer1")
        consumer2 = ThreadConsumer(queue, "consumer2")

        thread {
            printFormatMsg("======> ThreadProducerConsumer begin")
            val before = System.currentTimeMillis()
            producer.start()
            consumer1.start()
            consumer2.start()
            producer.join()
            consumer1.join()
            consumer2.join()
            printFormatMsg("ThreadProducerConsumer time = ${System.currentTimeMillis() - before}")
        }
    }
}