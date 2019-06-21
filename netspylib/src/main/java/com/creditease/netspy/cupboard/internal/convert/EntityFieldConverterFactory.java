
package com.creditease.netspy.cupboard.internal.convert;

import android.content.ContentValues;
import android.database.Cursor;
import com.creditease.netspy.cupboard.Cupboard;
import com.creditease.netspy.cupboard.convert.EntityConverter;
import com.creditease.netspy.cupboard.convert.EntityConverter.ColumnType;
import com.creditease.netspy.cupboard.convert.FieldConverter;
import com.creditease.netspy.cupboard.convert.FieldConverterFactory;

import java.lang.reflect.Type;

public class EntityFieldConverterFactory implements FieldConverterFactory {

    private static class EntityFieldConverter implements FieldConverter<Object> {
        private final Class<Object> entityClass;
        private final EntityConverter<Object> mEntityConverter;

        public EntityFieldConverter(Class<Object> clz, EntityConverter<?> entityConverter) {
            this.mEntityConverter = (EntityConverter<Object>) entityConverter;
            this.entityClass = clz;
        }

        @Override
        public Object fromCursorValue(Cursor cursor, int columnIndex) {
            long id = cursor.getLong(columnIndex);
            Object entity;
            try {
                entity = entityClass.newInstance();
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            }
            mEntityConverter.setId(id, entity);
            return entity;
        }

        @Override
        public void toContentValue(Object value, String key, ContentValues values) {
            values.put(key, mEntityConverter.getId(value));
        }

        @Override
        public ColumnType getColumnType() {
            return ColumnType.INTEGER;
        }
    }

    @Override
    public FieldConverter<?> create(Cupboard cupboard, Type type) {
        if (!(type instanceof Class)) {
            return null;
        }
        if (cupboard.isRegisteredEntity((Class<?>) type)) {
            EntityConverter<?> converter = cupboard.getEntityConverter((Class<?>) type);
            return new EntityFieldConverter((Class<Object>) type, converter);
        }
        return null;
    }
}
