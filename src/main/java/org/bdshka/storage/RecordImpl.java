package org.bdshka.storage;

import java.util.Arrays;
import java.util.Objects;

// Строка, значения колонок которой хранятся в массиве.
public final class RecordImpl implements Record {

    private final Schema schema;
    private final Object[] values;

    public RecordImpl(Schema schema, Object... values) {
        this.schema = Objects.requireNonNull(schema, "schema");
        Objects.requireNonNull(values, "values");
        if (values.length != schema.getColumns().size()) {
            throw new IllegalArgumentException(
                    "Expected " + schema.getColumns().size() + " values but got " + values.length);
        }
        this.values = values.clone();
    }

    @Override
    public Object get(int columnIndex) {
        return values[columnIndex];
    }

    @Override
    public Object get(String columnName) {
        return values[schema.getColumnIndex(columnName)];
    }

    public Schema getSchema() {
        return schema;
    }

    public int size() {
        return values.length;
    }

    public Object[] toArray() {
        return values.clone();
    }

    @Override
    public String toString() {
        return "Record" + Arrays.toString(values);
    }
}
