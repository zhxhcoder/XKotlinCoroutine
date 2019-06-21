package com.creditease.netspy.internal.support;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import com.creditease.netspy.internal.data.NetSpyContentProvider;

public class ClearTransactionsService extends IntentService {

    public ClearTransactionsService() {
        super("NetSpy-ClearTransactionsService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        getContentResolver().delete(NetSpyContentProvider.TRANSACTION_URI, null, null);
        NotificationHelper.clearBuffer();
        NotificationHelper notificationHelper = new NotificationHelper(this);
        notificationHelper.dismiss();
    }
}