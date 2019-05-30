package com.zhxh.coroutines.kotlincoroutine

import java.text.SimpleDateFormat
import java.util.*

val format = SimpleDateFormat("mm:ss.SSS")

var COLLECTOR: ((text: CharSequence) -> Unit)? = null

inline fun printFormatMsg(msg: String) {
    val log = "${format.format(Date())} - $msg「 ${Thread.currentThread().name}」"
    println(log)
    COLLECTOR?.invoke(log)
}