package com.zhxh.coroutines.net

import okhttp3.FormBody
import okhttp3.Interceptor
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject

import java.io.IOException
import java.util.UUID

class KotlinNetInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        var oldRequest = chain.request()
        // 加header
        oldRequest = oldRequest.newBuilder().addHeader(
            "",
            UUID.randomUUID().toString()
        ).build()

        // 加公共参数
        val json = JSONObject()
        var method = ""
        if (oldRequest.body() is FormBody) {
            val oldFormBody = oldRequest.body() as FormBody?
            if (oldFormBody != null) {
                for (i in 0 until oldFormBody.size()) {
                    if ("method" == oldFormBody.name(i)) {
                        method = oldFormBody.value(i)
                    } else {
                        try {
                            json.put(oldFormBody.name(i), oldFormBody.value(i))
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }

        val dataJson = JSONObject()
        try {
            dataJson.put("method", method)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        val signString = "data" + "=" + dataJson.toString() + "&" +
                "secret"

        val formBodybuilder = FormBody.Builder()
        formBodybuilder.add("data", dataJson.toString())
        formBodybuilder.add("sign", signString)
        // 替换请求
        val newRequest = oldRequest.newBuilder().post(formBodybuilder.build()).build()
        return chain.proceed(newRequest)
    }

}
