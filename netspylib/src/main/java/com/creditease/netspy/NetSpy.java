package com.creditease.netspy;

import android.content.Context;
import android.content.Intent;

import com.creditease.netspy.internal.ui.MainActivity;

/**
 * NetSpy utilities.
 */
public class NetSpy {

    /**
     * Get an Intent to launch the NetSpy UI directly.
     *
     * @param context A Context.
     * @return An Intent for the main NetSpy Activity that can be started with {@link Context#startActivity(Intent)}.
     */
    public static Intent getLaunchIntent(Context context) {
        return new Intent(context, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }
}