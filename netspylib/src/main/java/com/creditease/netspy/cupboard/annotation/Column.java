package com.creditease.netspy.cupboard.annotation;

import android.provider.ContactsContract;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation interface that allows one to decouple a field name from a column
 * name, by specifying the column name in an Annotation. This is particularly
 * useful when working with existing data like the {@link ContactsContract} ContentProvider as it utilises
 * generic column names (e.g. data1, data2,...,data15) which map to various
 * aliases depending on the mime type in use for a given row.
 * <p/>
 * Note that annotations are not processed by default. To enable processing of annotations construct an instance of Cupboard using {@link com.creditease.netspy.cupboard.CupboardBuilder} and call {@link com.creditease.netspy.cupboard.CupboardBuilder#useAnnotations()} <br/>
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = ElementType.FIELD)
public @interface Column {
    String value();
}
