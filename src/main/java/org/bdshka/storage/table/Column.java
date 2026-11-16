package org.bdshka.storage.table;

public final class Column {

    private final String name;
    private final ColumnType type;
    private final int position; // позиция в таблице
    private final boolean nullable;
    private final Object defaultValue;
    private final int maxLength; // для строковых
    private final boolean primaryKey;
    private final boolean unique;


    public Column(String name, ColumnType type, int position, boolean nullable, Object defaultValue, int maxLength, boolean primaryKey, boolean unique) {
        this.name = name;
        this.type = type;
        this.position = position;
        this.nullable = nullable;
        this.defaultValue = defaultValue;
        this.maxLength = maxLength;
        this.primaryKey = primaryKey;
        this.unique = unique;
    }

    public String getName() {
        return name;
    }

    public ColumnType getType() {
        return type;
    }

    public int getPosition() {
        return position;
    }

    public boolean isNullable() {
        return nullable;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public boolean isUnique() {
        return unique;
    }
}
