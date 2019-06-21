
package com.creditease.netspy.cupboard.convert;

import android.content.ContentValues;
import android.database.Cursor;

import com.creditease.netspy.cupboard.convert.EntityConverter.ColumnType;

/**
 * Converts a field of type T to a column representation and vice versa
 *
 * @param <T> the type
 */
public interface FieldConverter<T> {
    /**
     * Convert a cursor value at the specified index to an instance of T
     *
     * @param cursor      the cursor
     * @param columnIndex the index of the requested value in the cursor
     * @return the value or null
     */
    public T fromCursorValue(Cursor cursor, int columnIndex);

    /**
     * Convert an instance of T to a value that can be stored in a ContentValues object
     *
     * @param value  the value
     * @param key    the key to store the value under
     * @param values the content values to store the value
     */
    public void toContentValue(T value, String key, ContentValues values);

    /**
     * Return the column type
     *
     * @return the column type or null if this field type should be ignored.
     */
    public ColumnType getColumnType();
}
