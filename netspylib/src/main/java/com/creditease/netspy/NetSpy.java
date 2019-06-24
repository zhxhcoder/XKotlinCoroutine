package com.creditease.netspy;

import android.content.Context;
import android.content.Intent;

import com.creditease.netspy.internal.ui.NetSpyHomeActivity;

/**
 * Created by zhxh on 2018/11/12
 * NetSpy 工具类.
 */
public class NetSpy {

    public static Intent getLaunchIntent(Context context) {
        if (NetSpyInterceptor.isNetSpy) {
            return new Intent(context, NetSpyHomeActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        } else {
            return new Intent();
        }
    }
}