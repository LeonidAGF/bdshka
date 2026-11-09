package org.bdshka.storage.table;

import org.bdshka.storage.Cursor;
import org.bdshka.storage.Record;
import org.bdshka.storage.RowCursor;
import org.bdshka.storage.Schema;

public interface Table {

    String name();
    Schema getSchema();
    RowCursor scan();
    Cursor lookup(Key key);
    RowId findRowId(Key key);
    RowId insert(Record data);
    void update(RowId rowId, Record data);
    void delete(RowId rowId);
}