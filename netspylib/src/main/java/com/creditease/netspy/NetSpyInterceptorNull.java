package com.creditease.netspy;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.creditease.netspy.internal.data.NetSpyContentProvider;
import com.creditease.netspy.internal.data.HttpTransaction;
import com.creditease.netspy.internal.data.LocalCupboard;
import com.creditease.netspy.internal.support.NotificationHelper;
import com.creditease.netspy.internal.support.RetentionManager;

import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.http.HttpHeaders;
import okio.Buffer;
import okio.BufferedSource;
import okio.GzipSource;
import okio.Okio;

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
