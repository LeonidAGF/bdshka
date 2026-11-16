package org.bdshka.storage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.bdshka.storage.table.Column;
import org.bdshka.storage.table.ColumnType;
import org.junit.jupiter.api.Test;

class RecordImplTest {

    private static Schema schema() {
        return new SchemaImpl(List.of(
                new Column("id", ColumnType.INT, 0, false, null, 0, true, true),
                new Column("name", ColumnType.VARCHAR, 1, true, null, 32, false, false)
        ));
    }

    @Test
    void readsValuesByIndexAndName() {
        Schema schema = schema();
        Record record = new RecordImpl(schema, 10, "Alice");

        assertEquals(10, record.get(0));
        assertEquals("Alice", record.get(1));
        assertEquals("Alice", record.get("NAME"));
        assertEquals(2, ((RecordImpl) record).size());
        assertSame(schema, ((RecordImpl) record).getSchema());
    }

    @Test
    void allowsNullForNullableColumn() {
        Record record = new RecordImpl(schema(), 10, null);

        assertNull(record.get("name"));
    }

    @Test
    void rejectsWrongValueCount() {
        Schema schema = schema();

        assertThrows(IllegalArgumentException.class, () -> new RecordImpl(schema, 10));
        assertThrows(IllegalArgumentException.class,
                () -> new RecordImpl(schema, 10, "Alice", "extra"));
    }

    @Test
    void rejectsUnknownColumn() {
        Record record = new RecordImpl(schema(), 10, "Alice");

        assertThrows(IllegalArgumentException.class, () -> record.get("missing"));
    }

    @Test
    void copiesSourceAndReturnedArrays() {
        Object[] values = {10, "Alice"};
        RecordImpl record = new RecordImpl(schema(), values);

        values[0] = 99;
        Object[] returned = record.toArray();
        returned[1] = "Bob";

        assertArrayEquals(new Object[] {10, "Alice"}, record.toArray());
    }
}
