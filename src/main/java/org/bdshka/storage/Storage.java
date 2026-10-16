package org.bdshka.storage;

import org.bdshka.storage.table.Table;

public interface Storage {
    Table openTable(String name);
    void createTable(Schema schema);
    void dropTable(String name);
}
