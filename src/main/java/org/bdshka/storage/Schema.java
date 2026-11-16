package org.bdshka.storage;

import java.util.List;
import org.bdshka.storage.table.Column;

public interface Schema {

    List<Column> getColumns();
    Column getColumn(String name);
    int getColumnIndex(String name);
    List<Integer> getPrimaryKeyIndexes();
}