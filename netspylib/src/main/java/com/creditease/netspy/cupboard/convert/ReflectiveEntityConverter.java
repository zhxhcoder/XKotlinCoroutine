package com.creditease.netspy.cupboard.convert;

import android.content.ContentValues;
import android.database.Cursor;
import android.provider.BaseColumns;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.creditease.netspy.cupboard.Cupboard;
import com.creditease.netspy.cupboard.annotation.Ignore;
import com.creditease.netspy.cupboard.annotation.Index;

/**
 * The default {@link EntityConverter}
 */
public class ReflectiveEntityConverter<T> implements EntityConverter<T> {

    /**
     * The {@link Cupboard} instance for this converter
     */
    protected final Cupboard mCupboard;
    protected final Class<T> mEntityClass;
    private final List<Column> mColumns;
    private final Property[] mProperties;
    private final boolean mUseAnnotations;
    private Property mIdProperty;

    public ReflectiveEntityConverter(Cupboard cupboard, Class<T> entityClass) {
        this(cupboard, entityClass, Collections.<String>emptyList(), Collections.<Column>emptyList());
    }

    public ReflectiveEntityConverter(Cupboard cupboard, Class<T> entityClass, Collection<String> ignoredFieldsNames) {
        this(cupboard, entityClass, ignoredFieldsNames, Collections.<Column>emptyList());
    }

    /**
     * Constructor suitable for {@link com.creditease.netspy.cupboard.convert.EntityConverterFactory}s that only need minor
     * changes to the default behavior of this converter, not requiring a sub class.
     *
     * @param cupboard          the cupboard instance
     * @param entityClass       the entity class
     * @param ignoredFieldNames a collection of field names that should be ignored as an alternative to implementing {@link #isIgnored(Field)}
     * @param additionalColumns a collection of additional columns that will be requested from the cursor
     */
    public ReflectiveEntityConverter(Cupboard cupboard, Class<T> entityClass, Collection<String> ignoredFieldNames, Collection<Column> additionalColumns) {
        mCupboard = cupboard;
        mUseAnnotations = cupboard.isUseAnnotations();
        Field[] fields = getAllFields(entityClass);
        ArrayList<Column> columns = new ArrayList<Column>(fields.length);
        this.mEntityClass = entityClass;
        List<Property> properties = new ArrayList<Property>();
        for (Field field : fields) {
            if (ignoredFieldNames.contains(field.getName()) || isIgnored(field)) {
                continue;
            }
            Type type = field.getGenericType();
            FieldConverter<?> converter = getFieldConverter(field);
            if (converter == null) {
                throw new IllegalArgumentException("Do not know how to convert field " + field.getName() + " in entity " + entityClass.getName() + " of type " + type);
            }
            if (converter.getColumnType() == null) {
                continue;
            }
            Property prop = new Property();
            prop.field = field;
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            prop.name = getColumn(field);
            prop.type = field.getType();
            prop.fieldConverter = (FieldConverter<Object>) converter;
            prop.columnType = isReadOnlyColumn(field) ? ColumnType.JOIN : converter.getColumnType();
            properties.add(prop);
            if (BaseColumns._ID.equals(prop.name)) {
                mIdProperty = prop;
            }
            columns.add(new Column(prop.name, prop.columnType, getIndexes(field)));
        }
        columns.addAll(additionalColumns);
        this.mColumns = Collections.unmodifiableList(columns);
        this.mProperties = properties.toArray(new Property[properties.size()]);
    }

    private static String getTable(Class<?> clz) {
        return clz.getSimpleName();
    }

    /**
     * Get a {@link FieldConverter} for the specified field. This allows for subclasses
     * to provide a specific {@link FieldConverter} for a property. The default implementation
     * simply calls {@link Cupboard#getFieldConverter(Type)}
     *
     * @param field the field
     * @return the field converter
     */
    protected FieldConverter<?> getFieldConverter(Field field) {
        return mCupboard.getFieldConverter(field.getGenericType());
    }

    /**
     * Get all fields for the given class, including inherited fields. Note that no
     * attempts are made to deal with duplicate field names.
     *
     * @param clz the class to get the fields for
     * @return the fields
     */
    private Field[] getAllFields(Class<?> clz) {
        // optimize for the case where an entity is not inheriting from a base class.
        if (clz.getSuperclass() == null) {
            return clz.getDeclaredFields();
        }
        List<Field> fields = new ArrayList<Field>(256);
        Class<?> c = clz;
        do {
            Field[] f = c.getDeclaredFields();
            fields.addAll(Arrays.asList(f));
            c = c.getSuperclass();
        } while (c != null);
        Field[] result = new Field[fields.size()];
        return fields.toArray(result);
    }

    /**
     * Check if a field should be ignored. This allows subclasses to ignore fields at their discretion.
     * <p/>
     * The default implementation ignores all static, final or transient fields and if
     * {@link Cupboard#isUseAnnotations()} returns true also checks for the {@link Ignore}
     * annotation.
     *
     * @param field the field
     * @return true if this field should be ignored, false otherwise
     */
    protected boolean isIgnored(Field field) {
        int modifiers = field.getModifiers();
        boolean ignored = Modifier.isFinal(modifiers) || Modifier.isStatic(modifiers) || Modifier.isTransient(modifiers);
        if (mUseAnnotations) {
            ignored = ignored || field.getAnnotation(Ignore.class) != null;
        }
        return ignored;
    }

    @Override
    public T fromCursor(Cursor cursor) {
        try {
            T result = mEntityClass.newInstance();
            int cols = cursor.getColumnCount();
            for (int index = 0; index < mProperties.length && index < cols; index++) {
                Property prop = mProperties[index];
                Class<?> type = prop.type;
                if (cursor.isNull(index)) {
                    if (!type.isPrimitive()) {
                        prop.field.set(result, null);
                    }
                } else {
                    prop.field.set(result, prop.fieldConverter.fromCursorValue(cursor, index));
                }
            }
            return result;
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void toValues(T object, ContentValues values) {
        for (Property prop : mProperties) {
            if (prop.columnType == ColumnType.JOIN) {
                continue;
            }
            try {
                Object value = prop.field.get(object);
                if (value == null) {
                    if (!prop.name.equals(BaseColumns._ID)) {
                        values.putNull(prop.name);
                    }
                } else {
                    prop.fieldConverter.toContentValue(value, prop.name, values);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public List<Column> getColumns() {
        return mColumns;
    }

    /**
     * Return if the specified field is read only; meaning that it will never be stored, but only
     * read from if it exists in the database. A read only field will not be created when calling {@link com.creditease.netspy.cupboard.DatabaseCompartment#createTables()}
     *
     * @param field the field to check
     * @return true if this field should be read only (and of {@link ColumnType#JOIN}), false otherwise
     */
    protected boolean isReadOnlyColumn(Field field) {
        return false;
    }

    @Override
    public void setId(Long id, T instance) {
        if (mIdProperty != null) {
            try {
                mIdProperty.field.set(instance, id);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public Long getId(T instance) {
        if (mIdProperty != null) {
            try {
                return (Long) mIdProperty.field.get(instance);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    /**
     * Return the column name based on the field supplied. If annotation
     * processing is enabled for this converter and the field is annotated with
     * a {@link com.creditease.netspy.cupboard.annotation.Column} annotation, then
     * the column name is taken from the annotation. In all other cases the
     * column name is simply the name of the field.
     *
     * @param field the entity field
     * @return the database column name for this field
     */
    protected String getColumn(Field field) {
        if (mUseAnnotations) {
            com.creditease.netspy.cupboard.annotation.Column column = field
                    .getAnnotation(com.creditease.netspy.cupboard.annotation.Column.class);
            if (column != null) {
                return column.value();
            }
        }
        return field.getName();
    }

    protected Index getIndexes(Field field) {
        if (mUseAnnotations) {
            Index index = field
                    .getAnnotation(Index.class);
            if (index != null) {
                return index;
            }
        }
        return null;
    }

    @Override
    public String getTable() {
        return getTable(mEntityClass);
    }

    private static class Property {
        Field field;
        String name;
        Class<?> type;
        FieldConverter<Object> fieldConverter;
        ColumnType columnType;
    }

}
