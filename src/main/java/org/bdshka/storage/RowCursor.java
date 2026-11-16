package org.bdshka.storage;

import org.bdshka.storage.table.RowId;

public interface RowCursor extends AutoCloseable {

    record Entry(RowId rowId, Record record) {}

    boolean hasNext();

    Entry next();

    @Override
    void close();
}