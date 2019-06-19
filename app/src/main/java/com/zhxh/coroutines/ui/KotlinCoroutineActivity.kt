package com.zhxh.coroutines.ui

import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.zhxh.coroutines.R
import com.zhxh.coroutines.kotlincoroutine.*
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

class KotlinCoroutineActivity: FragmentActivity() {

    companion object {
        val TEST_CASE = listOf(
            CommonUsage,
            CompoundUsage,
            ChannelUsage,
            DispatcherUsage,
            CancelUsage,
            ThreadProducerConsumer,
            CoroutineProducerConsumer,
            ExceptionHandleUsage,
            Callback2CoroutineUsage,
            SuspendOrBlocking
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kotlin_coroutine)
        val spinner = findViewById<Spinner>(R.id.spinner1)
        spinner.adapter = MySpinnerAdapter()

        val logText = findViewById<TextView>(R.id.log_text)

        findViewById<Button>(R.id.go).setOnClickListener {
            (spinner.selectedItem as? ITestCase)?.test()
        }

        findViewById<Button>(R.id.clear).setOnClickListener {
            logText.text = ""
        }

        val weakTextView = WeakReference(logText)
        COLLECTOR = { log ->
            GlobalScope.launch(Main) {
                weakTextView.get()?.apply {
                    append("$log\n")
                }
            }
        }

    }

    inner class MySpinnerAdapter: BaseAdapter() {

        override fun isEmpty() = false

        override fun getCount() = TEST_CASE.size

        override fun getItem(position: Int): Any {
            return TEST_CASE[position]
        }

        override fun getItemId(position: Int) = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?) = buildText(getItem(position).javaClass.simpleName)

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?) = buildText(getItem(position).javaClass.simpleName)

        private fun buildText(name: String): TextView {
            val text = TextView(this@KotlinCoroutineActivity)
            text.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 100)
            text.text = name
            text.setPadding(20, 0, 0, 0)
            text.gravity = Gravity.LEFT or Gravity.CENTER_VERTICAL
            return text
        }

    }
}