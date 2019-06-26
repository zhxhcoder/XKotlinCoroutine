package com.zhxh.coroutines.ui

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.creditease.netspy.NetSpyHelper
import com.zhxh.coroutines.R
import com.zhxh.coroutines.ui.netspy.MainNetSpyActivity
import kotlinx.android.synthetic.main.activity_guide.*

class GuideActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guide)

        btn_main.setOnClickListener {
            startActivity(Intent(this@GuideActivity, MainMVPActivity::class.java))
        }

        btn_coroutine.setOnClickListener {
            startActivity(Intent(this@GuideActivity, KotlinCoroutineActivity::class.java))
        }
        btn_netspy.setOnClickListener {
            startActivity(Intent(this@GuideActivity, MainNetSpyActivity::class.java))
        }
        btn_netspy_open.setOnClickListener {

            NetSpyHelper.debug(true)
        }
    }
}
