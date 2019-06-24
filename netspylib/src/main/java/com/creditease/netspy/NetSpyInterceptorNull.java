package com.creditease.netspy;

import android.content.Context;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

/**
 * Created by zhxh on 2019/06/22
 * No-op implementation.
 */
public final class NetSpyInterceptorNull implements Interceptor {
    public enum Period {
        ONE_HOUR,
        ONE_DAY,
        ONE_WEEK,
        FOREVER
    }

    public NetSpyInterceptorNull(Context context) {
    }

    public NetSpyInterceptorNull showNotification(boolean show) {
        return this;
    }

    public NetSpyInterceptorNull maxContentLength(long max) {
        return this;
    }

    public NetSpyInterceptorNull retainDataFor(Period period) {
        return this;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        return chain.proceed(request);
    }

}
