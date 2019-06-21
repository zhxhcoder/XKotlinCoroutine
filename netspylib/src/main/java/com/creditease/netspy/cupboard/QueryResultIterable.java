package com.creditease.netspy.cupboard;

import android.database.Cursor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import com.creditease.netspy.cupboard.PreferredColumnOrderCursorWrapper;
import com.creditease.netspy.cupboard.convert.EntityConverter;


public class QueryResultIterable<T> implements Iterable<T> {

    private final Cursor mCursor;
    private final EntityConverter<T> mTranslator;
    private final int mPosition;

    QueryResultIterable(Cursor cursor, EntityConverter<T> translator) {
        if (cursor.getPosition() > -1) {
            this.mPosition = cursor.getPosition();
        } else {
            this.mPosition = -1;
        }
        this.mCursor = cursor;
        this.mTranslator = translator;
    }

    @Override
    public Iterator<T> iterator() {
        mCursor.moveToPosition(mPosition);
        return new QueryResultIterator<T>(mCursor, mTranslator);
    }

    public void close() {
        if (!mCursor.isClosed()) {
            mCursor.close();
        }
    }

    public Cursor getCursor() {
        return mCursor;
    }

    public T get() {
        return get(true);
    }

    public T get(boolean close) {
        try {
            Iterator<T> itr = iterator();
            if (itr.hasNext()) {
                return itr.next();
            } else {
                return null;
            }
        } finally {
            if (close) {
                close();
            }
        }
    }

    /**
     * Return the result as a list. Only to be used if the resultset is to be expected of reasonable size. The underlying cursor will
     * be closed when this method returns.
     *
     * @return the result set as a list.
     */
    public List<T> list() {
        return list(true);
    }

    /**
     * Return the result as a list. Only to be used if the resultset is to be expected of reasonable size.
     *
     * @param close true if the underlying cursor should be closed, false otherwise
     * @return the result set as a list.
     */
    public List<T> list(boolean close) {
        List<T> result = new ArrayList<T>(mCursor.getCount());
        try {
            for (T obj : this) {
                result.add(obj);
            }
            return result;
        } finally {
            if (close) {
                close();
            }
        }
    }

    static class QueryResultIterator<E> implements Iterator<E> {
        private final Cursor mCursor;
        private final EntityConverter<E> mTranslator;
        private final int mCount;
        private int mPosition;

        public QueryResultIterator(Cursor cursor, EntityConverter<E> translator) {
            this.mCursor = new com.creditease.netspy.cupboard.PreferredColumnOrderCursorWrapper(cursor, translator.getColumns());
            this.mTranslator = translator;
            this.mPosition = cursor.getPosition();
            this.mCount = cursor.getCount();
            if (mPosition != -1) {
                mPosition--;
            }
        }

        @Override
        public boolean hasNext() {
            return mPosition < mCount - 1;
        }

        @Override
        public E next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            mCursor.moveToPosition(++mPosition);
            return mTranslator.fromCursor(mCursor);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

}
