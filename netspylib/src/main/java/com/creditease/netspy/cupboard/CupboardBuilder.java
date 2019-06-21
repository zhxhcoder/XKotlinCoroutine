
package com.creditease.netspy.cupboard;

import com.creditease.netspy.cupboard.convert.EntityConverterFactory;
import com.creditease.netspy.cupboard.convert.FieldConverter;
import com.creditease.netspy.cupboard.convert.FieldConverterFactory;

/**
 * Aids in creating specialized {@link Cupboard} instances
 */
public class CupboardBuilder {
    private Cupboard mCupboard;

    public CupboardBuilder() {
        mCupboard = new Cupboard();
    }

    /**
     * Create a builder based on an existing Cupboard instance. All entities and converters registered with this instance will
     * be registered with the instance that this builder is building.
     *
     * @param cupboard the instance to retrieve the registered entities and converters from.
     */
    public CupboardBuilder(Cupboard cupboard) {
        mCupboard = new Cupboard(cupboard);
        for (Class<?> entity : cupboard.getRegisteredEntities()) {
            mCupboard.register(entity);
        }
    }

    /**
     * Register a {@link EntityConverterFactory}
     *
     * @param factory the factory
     * @return the builder for chaining
     */
    public com.creditease.netspy.cupboard.CupboardBuilder registerEntityConverterFactory(EntityConverterFactory factory) {
        mCupboard.registerEntityConverterFactory(factory);
        return this;
    }

    /**
     * Register a {@link FieldConverterFactory}
     *
     * @param factory the factory
     * @return the builder for chaining
     */
    public com.creditease.netspy.cupboard.CupboardBuilder registerFieldConverterFactory(FieldConverterFactory factory) {
        mCupboard.registerFieldConverterFactory(factory);
        return this;
    }

    /**
     * Register a field converter
     *
     * @param fieldClass the field class
     * @param converter  the converter
     * @return the builder for chaining
     */
    public <T> com.creditease.netspy.cupboard.CupboardBuilder registerFieldConverter(Class<T> fieldClass, FieldConverter<T> converter) {
        mCupboard.registerFieldConverter(fieldClass, converter);
        return this;
    }

    /**
     * Enable the use of annotations
     *
     * @return the builder for chaining
     */
    public com.creditease.netspy.cupboard.CupboardBuilder useAnnotations() {
        mCupboard.setUseAnnotations(true);
        return this;
    }


    /**
     * Create the {@link Cupboard} instance.
     *
     * @return the Cupboard instance
     */
    public Cupboard build() {
        return mCupboard;
    }
}
