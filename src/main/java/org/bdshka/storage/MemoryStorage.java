package org.bdshka.storage;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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
    public Table createTable(String name, Schema schema) {
        requireTableName(name);
        Objects.requireNonNull(schema, "schema");

        Table table = new TableImpl(name, schema, pageSize);
        if (tables.putIfAbsent(name, table) != null) {
            throw new IllegalArgumentException("Table already exists: " + name);
        }
        return table;
    }

    @Override
    public void dropTable(String name, boolean ifExists) {
        requireTableName(name);
        if (tables.remove(name) == null && !ifExists) {
            throw new IllegalArgumentException("Table not found: " + name);
        }
    }

    @Override
    public Table getTable(String name) {
        requireTableName(name);
        Table table = tables.get(name);
        if (table == null) {
            throw new IllegalArgumentException("Table not found: " + name);
        }
        return table;
    }

    @Override
    public Optional<Table> findTable(String name) {
        requireTableName(name);
        return Optional.ofNullable(tables.get(name));
    }

    @Override
    public Collection<String> tableNames() {
        return List.copyOf(tables.keySet());
    }

    // Количество таблиц в хранилище.
    public int size() {
        return tables.size();
    }

    // Проверяет, есть ли таблица с таким именем.
    public boolean containsTable(String name) {
        requireTableName(name);
        return tables.containsKey(name);
    }

    private static void requireTableName(String name) {
        Objects.requireNonNull(name, "name");
        if (name.isBlank()) {
            throw new IllegalArgumentException("Table name must not be blank");
        }
    }
}
