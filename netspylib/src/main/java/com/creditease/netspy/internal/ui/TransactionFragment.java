package com.creditease.netspy.internal.ui;

import com.creditease.netspy.internal.data.HttpTransaction;

interface TransactionFragment {
    void transactionUpdated(HttpTransaction transaction);
}