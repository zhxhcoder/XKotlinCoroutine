package com.creditease.netspy.internal.data;

import com.creditease.netspy.cupboard.Cupboard;
import com.creditease.netspy.cupboard.CupboardBuilder;

public class LocalCupboard {

    private static Cupboard cupboard;

    static {
        getInstance().register(HttpTransaction.class);
    }

    public static Cupboard getInstance() {
        if (cupboard == null) {
            cupboard = new CupboardBuilder().build();
        }
        return cupboard;
    }

    public static Cupboard getAnnotatedInstance() {
        return new CupboardBuilder(getInstance())
                .useAnnotations()
                .build();
    }

    private LocalCupboard() {
    }
}
