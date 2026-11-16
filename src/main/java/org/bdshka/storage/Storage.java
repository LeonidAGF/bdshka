package org.bdshka.storage;

import java.util.Collection;
import java.util.Optional;
import org.bdshka.storage.table.Table;

public interface Storage {

    Table createTable(String name, Schema schema);
    void dropTable(String name, boolean ifExists);
    Table getTable(String name);
    Optional<Table> findTable(String name);
    Collection<String> tableNames();
}