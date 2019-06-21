package com.creditease.netspy.cupboard.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation interface that allows one to order columns for a composite index (if another column in the same table shares
 * the same index name in {@link com.creditease.netspy.cupboard.annotation.Index})
 * For more information see
 * <a href="http://stackoverflow.com/questions/2292662/how-important-is-the-order-of-columns-in-indexes}"><b>Why order matters?</b></a>
 * <p/>
 * Note that annotations are not processed by default. To enable processing of annotations construct an instance of Cupboard using {@link com.creditease.netspy.cupboard.CupboardBuilder} and call {@link com.creditease.netspy.cupboard.CupboardBuilder#useAnnotations()} <br/>
 */
@Retention(value = RetentionPolicy.RUNTIME)
public @interface CompositeIndex {
    public static final boolean DEFAULT_ASCENDING = true;
    public static final int DEFAULT_ORDER = 0;
    public static final String DEFAULT_INDEX_NAME = "";

    /**
     * @return whether a ascending index should be created on this column. By default it is true.
     */
    boolean ascending() default DEFAULT_ASCENDING;

    /**
     * @return order of the column in the composite index
     */
    int order() default DEFAULT_ORDER;

    /**
     * @return name of the composite index if .
     */
    String indexName();
}