package org.bdshka.storage;

import org.bdshka.storage.table.Column;

import java.util.List;

public interface Schema {
    List<Column> getColumns();
    Column getColumn(String name);
    int getColumnIndex(String name);
}
