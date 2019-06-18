package com.zhxh.coroutines.ui

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.AndroidException
import android.util.Log
import android.view.View
import android.widget.Toast
import com.zhxh.coroutines.R
import com.zhxh.coroutines.base.BasePresenter
import com.zhxh.coroutines.base.BaseView
import com.zhxh.coroutines.base.MvpPresenter
import com.zhxh.coroutines.model.Repository
import com.zhxh.coroutines.model.TAG
import com.zhxh.coroutines.entities.CommonBean
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main_mvp.*
import kotlinx.coroutines.launch

/**
 * Created by zhxh on 2019/05/19
 */
class MainContract {
    interface View : BaseView<String> {
        fun showLoadingView()
        fun showLoadingSuccessView(list: List<CommonBean>)
        fun showLoadingErrorView()
    }

    interface Presenter : MvpPresenter<View> {
        fun syncWithContext()
        fun syncNoneWithContext()
        fun asyncWithContextForAwait()
        fun asyncWithContextForNoAwait()
        fun adapterCoroutineQuery()
        fun rxJavaQuery()
    }
}

class MainPresenter : MainContract.Presenter, BasePresenter<MainContract.View>() {

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
                val results = Repository.querySyncNoneWithContext()
                view.showLoadingSuccessView(results)
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
                val results = Repository.queryAsyncWithContextForAwait()
                view.showLoadingSuccessView(results)
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
                val results = Repository.queryAsyncWithContextForNoAwait()
                view.showLoadingSuccessView(results)
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
                val results = Repository.adapterCoroutineQuery()
                view.showLoadingSuccessView(results)
            } catch (e: Throwable) {
                view.showLoadingErrorView()
            } finally {
                Log.d(TAG, "耗时：${System.currentTimeMillis() - time}")
            }
        }
    }

    override fun rxJavaQuery() {

        val disposable = Repository.rxJavaQuery()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                Log.d("TestCoroutine", "-disposable->" + it.data[0].name)
            }
    }
}

class MainMVPActivity : AppCompatActivity(), MainContract.View {
    override fun loadingIndicator(show: Boolean, msg: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun initView(data: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun destroyView() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private val presenter = MainPresenter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_mvp)
        presenter.subscribe(this)
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
        rxjavaBtn.setOnClickListener {
            presenter.rxJavaQuery()
        }
    }

    override fun showLoadingView() {
        loadingBar.showSelf()
    }

    override fun showLoadingSuccessView(list: List<CommonBean>) {
        loadingBar.hideSelf()
        textView.text = "请求结束+"
        Toast.makeText(this, "加载成功", Toast.LENGTH_SHORT).show()
        Log.d(TAG, "请求结果：$list")
    }

    override fun showLoadingErrorView() {
        loadingBar.hideSelf()
        Toast.makeText(this, "加载失败", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.unsubscribe()
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
