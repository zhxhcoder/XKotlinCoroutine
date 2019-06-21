
package com.creditease.netspy.cupboard;

import com.creditease.netspy.cupboard.Cupboard;
import com.creditease.netspy.cupboard.convert.EntityConverter;

class BaseCompartment {
    protected final Cupboard mCupboard;

    protected BaseCompartment(Cupboard cupboard) {
        this.mCupboard = cupboard;
    }

    protected <T> EntityConverter<T> getConverter(Class<T> clz) {
        return mCupboard.getEntityConverter(clz);
    }
}
