package com.creditease.netspy.cupboard.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation interface that allows one to mark as to be ignored. This is particularly useful when the transient field
 * modifier, which has the same operational effect, would cause unwanted side effects, such as with other serialization
 * methods.
 * <p/>
 * Note that annotations are not processed by default. To enable processing of annotations construct an instance of Cupboard using {@link com.creditease.netspy.cupboard.CupboardBuilder} and call {@link com.creditease.netspy.cupboard.CupboardBuilder#useAnnotations()} <br/>
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = ElementType.FIELD)
public @interface Ignore {
}
