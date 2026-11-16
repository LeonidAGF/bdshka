package org.bdshka.storage;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.bdshka.storage.table.Table;
import org.bdshka.storage.table.TableImpl;

// Класс, управляющий таблицами.
public final class MemoryStorage implements Storage {

    private final Map<String, Table> tables = new ConcurrentHashMap<>();
    private final int pageSize;

    public MemoryStorage() {
        this(TableImpl.DEFAULT_PAGE_SIZE);
    }

    public MemoryStorage(int pageSize) {
        if (pageSize < 1024) {
            throw new IllegalArgumentException("Page size is too small: " + pageSize);
        }
        this.pageSize = pageSize;
    }

    @Override
    public Table openTable(String name) {
        Objects.requireNonNull(name, "name");
        Table table = tables.get(name);
        if (table == null) {
            throw new IllegalArgumentException("Table not found: " + name);
        }
        return table;
    }

    @Override
    public void createTable(Schema schema) {
        Objects.requireNonNull(schema, "schema");
        String name = tableName(schema);
        if (tables.containsKey(name)) {
            throw new IllegalArgumentException("Table already exists: " + name);
        }
        tables.put(name, new TableImpl(schema, pageSize));
    }

    @Override
    public void dropTable(String name) {
        Objects.requireNonNull(name, "name");
        if (tables.remove(name) == null) {
            throw new IllegalArgumentException("Table not found: " + name);
        }
    }

    // Количество таблиц в хранилище.
    public int size() {
        return tables.size();
    }

    // Проверяет, есть ли таблица с таким именем.
    public boolean containsTable(String name) {
        Objects.requireNonNull(name, "name");
        return tables.containsKey(name);
    }

    private static String tableName(Schema schema) {
        if (schema.getColumns().isEmpty()) {
            throw new IllegalArgumentException("Schema must contain at least one column");
        }
        return schema.getColumns().get(0).getName() + "_table";
    }
}
