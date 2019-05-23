package com.zhxh.coroutines.ui

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.zhxh.coroutines.R
import com.zhxh.coroutines.base.BasePresenter
import com.zhxh.coroutines.base.MvpPresenter
import com.zhxh.coroutines.base.MvpView
import com.zhxh.coroutines.model.repository.Repository
import com.zhxh.coroutines.model.repository.TAG
import com.zhxh.coroutines.entities.CommonBean
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.launch
/**
 * Created by zhxh on 2019/05/19
 */
class MainContract {
    interface View: MvpView {
        fun showLoadingView()
        fun showLoadingSuccessView(granks: List<CommonBean>)
        fun showLoadingErrorView()
    }

    interface Presenter: MvpPresenter<View> {
        fun syncWithContext()
        fun syncNoneWithContext()
        fun asyncWithContextForAwait()
        fun asyncWithContextForNoAwait()
        fun adapterCoroutineQuery()
    }
}

class MainPresenter: MainContract.Presenter, BasePresenter<MainContract.View>() {

    override fun syncWithContext() {
        presenterScope.launch {
            val time = System.currentTimeMillis()
            view.showLoadingView()
            try {
                val resultList = Repository.querySyncWithContext()
                view.showLoadingSuccessView(resultList)
            } catch (e: Throwable) {
                view.showLoadingErrorView()
            } finally {
                Log.d(TAG, "耗时：${System.currentTimeMillis() - time}")
            }
        }
    }

    override fun syncNoneWithContext() {
        presenterScope.launch {
            val time = System.currentTimeMillis()
            view.showLoadingView()
            try {
                val ganks = Repository.querySyncNoneWithContext()
                view.showLoadingSuccessView(ganks)
            } catch (e: Throwable) {
                view.showLoadingErrorView()
            } finally {
                Log.d(TAG, "耗时：${System.currentTimeMillis() - time}")
            }
        }
    }

    override fun asyncWithContextForAwait() {
        presenterScope.launch {
            val time = System.currentTimeMillis()
            view.showLoadingView()
            try {
                val ganks = Repository.queryAsyncWithContextForAwait()
                view.showLoadingSuccessView(ganks)
            } catch (e: Throwable) {
                view.showLoadingErrorView()
            } finally {
                Log.d(TAG, "耗时：${System.currentTimeMillis() - time}")
            }
        }
    }

    override fun asyncWithContextForNoAwait() {
        presenterScope.launch {
            val time = System.currentTimeMillis()
            view.showLoadingView()
            try {
                val ganks = Repository.queryAsyncWithContextForNoAwait()
                view.showLoadingSuccessView(ganks)
            } catch (e: Throwable) {
                view.showLoadingErrorView()
            } finally {
                Log.d(TAG, "耗时：${System.currentTimeMillis() - time}")
            }
        }
    }

    override fun adapterCoroutineQuery() {
        presenterScope.launch {
            val time = System.currentTimeMillis()
            view.showLoadingView()
            try {
                val ganks = Repository.adapterCoroutineQuery()
                view.showLoadingSuccessView(ganks)
            } catch (e: Throwable) {
                view.showLoadingErrorView()
            } finally {
                Log.d(TAG, "耗时：${System.currentTimeMillis() - time}")
            }
        }
    }
}

class MainActivity : AppCompatActivity(), MainContract.View {
    private val presenter = MainPresenter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        presenter.attachView(this)
        syncWithContextBtn.setOnClickListener {
            presenter.syncWithContext()
        }
        syncNoneWithContext.setOnClickListener {
            presenter.syncNoneWithContext()
        }
        asyncWithContextForAwait.setOnClickListener {
            presenter.asyncWithContextForAwait()
        }
        asyncWithContextForNoAwait.setOnClickListener {
            presenter.asyncWithContextForNoAwait()
        }
        adapterBtn.setOnClickListener {
            presenter.adapterCoroutineQuery()
        }
    }

    override fun showLoadingView() {
        loadingBar.showSelf()
    }

    override fun showLoadingSuccessView(resultList: List<CommonBean>) {
        loadingBar.hideSelf()
        textView.text = "请求结束+"
        Toast.makeText(this, "加载成功", Toast.LENGTH_SHORT).show()
        Log.d(TAG, "请求结果：$resultList")
    }

    override fun showLoadingErrorView() {
        loadingBar.hideSelf()
        Toast.makeText(this, "加载失败", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()
    }

    override fun onBackPressed() {
        finish()
    }
}

fun View.showSelf() {
    this.visibility = View.VISIBLE
}

fun View.hideSelf() {
    this.visibility = View.GONE
}
