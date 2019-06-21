
package com.creditease.netspy.cupboard;

import android.content.ContentValues;

import com.creditease.netspy.cupboard.BaseCompartment;
import com.creditease.netspy.cupboard.Cupboard;
import com.creditease.netspy.cupboard.convert.EntityConverter;

public class EntityCompartment<T> extends BaseCompartment {

    private final EntityConverter<T> mConverter;

    protected EntityCompartment(Cupboard cupboard, Class<T> clz) {
        super(cupboard);
        mConverter = getConverter(clz);
    }

    /**
     * Get the table name for this entity class
     *
     * @return the table name
     */
    public String getTable() {
        return mConverter.getTable();
    }

    /**
     * Convert an entity to {@link ContentValues}
     *
     * @param entity the entity
     * @return the values
     */
    public ContentValues toContentValues(T entity) {
        return toContentValues(entity, null);
    }

    /**
     * Convert an entity to {@link ContentValues}
     *
     * @param entity the entity
     * @param values the content values, may be null
     * @return the values
     */
    public ContentValues toContentValues(T entity, ContentValues values) {
        if (values == null) {
            values = new ContentValues(mConverter.getColumns().size());
        }
        mConverter.toValues(entity, values);
        return values;
    }
}
