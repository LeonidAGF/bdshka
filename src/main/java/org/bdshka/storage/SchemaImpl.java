package org.bdshka.storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.bdshka.storage.table.Column;

// Неизменяемая схема таблицы.
public final class SchemaImpl implements Schema {

    private final List<Column> columns;
    private final Map<String, Integer> columnIndexes;

    public SchemaImpl(List<Column> columns) {
        Objects.requireNonNull(columns, "columns");
        if (columns.isEmpty()) {
            throw new IllegalArgumentException("Schema must contain at least one column");
        }

        this.columns = List.copyOf(columns);
        this.columnIndexes = new HashMap<>();

        for (int i = 0; i < this.columns.size(); i++) {
            Column column = Objects.requireNonNull(this.columns.get(i), "column");
            if (column.getName() == null || column.getName().isBlank()) {
                throw new IllegalArgumentException("Column name must not be blank");
            }
            String normalizedName = normalize(column.getName());
            if (columnIndexes.putIfAbsent(normalizedName, i) != null) {
                throw new IllegalArgumentException("Duplicate column: " + column.getName());
            }
        }
    }

    @Override
    public List<Column> getColumns() {
        return columns;
    }

    @Override
    public Column getColumn(String name) {
        int index = getColumnIndex(name);
        return columns.get(index);
    }

    @Override
    public int getColumnIndex(String name) {
        Objects.requireNonNull(name, "name");
        Integer index = columnIndexes.get(normalize(name));
        if (index == null) {
            throw new IllegalArgumentException("Unknown column: " + name);
        }
        return index;
    }

    // Возвращает индексы колонок, образующих первичный ключ, в порядке схемы.
    public List<Integer> getPrimaryKeyIndexes() {
        List<Integer> indexes = new ArrayList<>();
        for (int i = 0; i < columns.size(); i++) {
            if (columns.get(i).isPrimaryKey()) {
                indexes.add(i);
            }
        }
        return List.copyOf(indexes);
    }

    private static String normalize(String name) {
        return name.toLowerCase(Locale.ROOT);
    }
}
