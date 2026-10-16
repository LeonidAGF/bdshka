package org.bdshka.storage.table;

import org.bdshka.storage.Cursor;
import org.bdshka.storage.Schema;

interface Table {
    Schema getSchema();
    Cursor scan();//проходимся просто по всему в таблице
    Cursor lookup(Key key); //поиск по ключу
    void insert(Record data);
    void update(RowId rowId, Record data);
    void delete(RowId rowId);
}