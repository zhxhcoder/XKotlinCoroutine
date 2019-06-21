
package com.creditease.netspy.cupboard.internal.convert;

import android.content.ContentValues;
import android.database.Cursor;
import com.creditease.netspy.cupboard.Cupboard;
import com.creditease.netspy.cupboard.convert.EntityConverter.ColumnType;
import com.creditease.netspy.cupboard.convert.FieldConverter;
import com.creditease.netspy.cupboard.convert.FieldConverterFactory;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class EnumFieldConverterFactory implements FieldConverterFactory {
    private static class EnumConverter<E extends Enum> implements FieldConverter<E> {

        private final Class<E> mEnumClass;

        public EnumConverter(Class<E> enumClass) {
            this.mEnumClass = enumClass;
        }

        @Override
        public E fromCursorValue(Cursor cursor, int columnIndex) {
            return (E) Enum.valueOf(mEnumClass, cursor.getString(columnIndex));
        }

        @Override
        public void toContentValue(E value, String key, ContentValues values) {
            values.put(key, value.toString());
        }

        @Override
        public ColumnType getColumnType() {
            return ColumnType.TEXT;
        }
    }

    @Override
    public FieldConverter<?> create(Cupboard cupboard, Type type) {
        // enum can also be declared as Enum<EnumType>
        if (type instanceof ParameterizedType) {
            if (((ParameterizedType) type).getRawType() == Enum.class) {
                type = ((ParameterizedType) type).getActualTypeArguments()[0];
            }
        }
        if (!(type instanceof Class)) {
            return null;
        }
        Class<?> clz = (Class<?>) type;
        if (clz.isEnum()) {
            return new EnumConverter<Enum>((Class<Enum>) clz);
        }
        return null;
    }
}
