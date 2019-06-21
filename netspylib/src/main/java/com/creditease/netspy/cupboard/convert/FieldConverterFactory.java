package com.creditease.netspy.cupboard.convert;

import java.lang.reflect.Type;

import com.creditease.netspy.cupboard.*;

/**
 * An field converter factory instantiates {@link FieldConverter}s. A single factory may support
 * multiple field types.
 */
public interface FieldConverterFactory {

    /**
     * Create a new FieldConverter for the given type
     *
     * @param cupboard the cupboard instance requesting the FieldConverter
     * @param type     the type of the field.
     * @return a suitable FieldConverter or null if the type is not supported by this factory.
     */
    public FieldConverter<?> create(Cupboard cupboard, Type type);
}
