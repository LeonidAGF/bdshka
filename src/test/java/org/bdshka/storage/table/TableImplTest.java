package org.bdshka.storage.table;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bdshka.storage.Cursor;
import org.bdshka.storage.Record;
import org.bdshka.storage.RecordImpl;
import org.bdshka.storage.Schema;
import org.bdshka.storage.SchemaImpl;
import org.junit.jupiter.api.Test;

class TableImplTest {

    private static Schema schema() {
        return new SchemaImpl(List.of(
                new Column("id", ColumnType.INT, 0, false, null, 0, true, true),
                new Column("name", ColumnType.VARCHAR, 1, true, null, 32, false, false),
                new Column("score", ColumnType.DOUBLE, 2, true, null, 0, false, false),
                new Column("active", ColumnType.BOOLEAN, 3, false, true, 0, false, false),
                new Column("created", ColumnType.TIMESTAMP, 4, true, null, 0, false, false),
                new Column("birthday", ColumnType.DATE, 5, true, null, 0, false, false)
        ));
    }

    private static Record row(Schema schema, int id, String name, double score) {
        return new RecordImpl(schema, id, name, score, true,
                LocalDateTime.of(2026, 10, 23, 12, 30), LocalDate.of(2000, 1, 2));
    }

    private static Record first(Table table, Key key) {
        Cursor cursor = table.lookup(key);
        try {
            assertTrue(cursor.hasNext());
            return cursor.next();
        } finally {
            cursor.close();
        }
    }

    @Test
    void insertsAndLooksUpByPrimaryKey() {
        Schema schema = schema();
        TableImpl table = new TableImpl(schema);

        RowId rowId = table.insert(row(schema, 1, "Alice", 10.5));

        assertNotNull(rowId);
        assertEquals(1, table.size());
        assertEquals(rowId, table.findRowId(Key.of(1)));

        Record record = first(table, Key.of(1));
        assertEquals(1, record.get("id"));
        assertEquals("Alice", record.get("name"));
        assertEquals(10.5, record.get("score"));
        assertEquals(LocalDate.of(2000, 1, 2), record.get("birthday"));
    }

    @Test
    void missingLookupReturnsEmptyCursor() {
        TableImpl table = new TableImpl(schema());

        Cursor cursor = table.lookup(Key.of(42));

        assertFalse(cursor.hasNext());
        assertNull(table.findRowId(Key.of(42)));
        cursor.close();
    }

    @Test
    void scansRowsPageByPage() {
        Schema schema = schema();
        TableImpl table = new TableImpl(schema, 96);
        int rowCount = 250;

        for (int id = 1; id <= rowCount; id++) {
            table.insert(row(schema, id, "name-" + id, id * 1.5));
        }

        assertTrue(table.pageCount() > 1);

        Set<Integer> seen = new HashSet<>();
        Cursor cursor = table.scan();
        try {
            while (cursor.hasNext()) {
                Record record = cursor.next();
                assertTrue(seen.add((Integer) record.get("id")));
            }
        } finally {
            cursor.close();
        }

        assertEquals(rowCount, seen.size());
        assertEquals(rowCount, table.size());
    }

    @Test
    void updatesRowInPlaceAndChangesPrimaryKey() {
        Schema schema = schema();
        TableImpl table = new TableImpl(schema);
        RowId rowId = table.insert(row(schema, 1, "Alice", 10.5));

        table.update(rowId, row(schema, 2, "Alice Updated", 20.5));

        assertFalse(table.lookup(Key.of(1)).hasNext());
        assertEquals(rowId, table.findRowId(Key.of(2)));
        Record record = first(table, Key.of(2));
        assertEquals("Alice Updated", record.get("name"));
        assertEquals(20.5, record.get("score"));
    }

    @Test
    void updatesRowWhenSerializedRowGrows() {
        Schema schema = schema();
        TableImpl table = new TableImpl(schema, 96);
        RowId rowId = table.insert(row(schema, 1, "a", 1.0));

        table.update(rowId, row(schema, 1, "a-longer-name-1234567890", 2.0));

        Record record = first(table, Key.of(1));
        assertEquals("a-longer-name-1234567890", record.get("name"));
        assertEquals(2.0, record.get("score"));
    }

    @Test
    void deleteRemovesRowAndFreesKey() {
        Schema schema = schema();
        TableImpl table = new TableImpl(schema);
        RowId rowId = table.insert(row(schema, 1, "Alice", 10.5));
        table.insert(row(schema, 2, "Bob", 11.5));

        table.delete(rowId);

        assertEquals(1, table.size());
        assertNull(table.findRowId(Key.of(1)));
        assertFalse(table.lookup(Key.of(1)).hasNext());
        assertEquals("Bob", first(table, Key.of(2)).get("name"));

        table.insert(row(schema, 1, "Alice Again", 12.5));
        assertEquals("Alice Again", first(table, Key.of(1)).get("name"));
    }

    @Test
    void rejectsDuplicateAndInvalidRows() {
        Schema schema = schema();
        TableImpl table = new TableImpl(schema);
        table.insert(row(schema, 1, "Alice", 10.5));

        assertThrows(IllegalArgumentException.class,
                () -> table.insert(row(schema, 1, "Another", 1.0)));
        assertThrows(IllegalArgumentException.class,
                () -> table.insert(new RecordImpl(schema, null, "No id", 1.0, true, null, null)));
        assertThrows(IllegalArgumentException.class,
                () -> table.insert(new RecordImpl(
                        schema, 2, "x".repeat(33), 1.0, true, null, null)));
    }

    @Test
    void supportsCompositePrimaryKeys() {
        Schema schema = new SchemaImpl(List.of(
                new Column("tenant", ColumnType.INT, 0, false, null, 0, true, true),
                new Column("id", ColumnType.INT, 1, false, null, 0, true, true),
                new Column("value", ColumnType.TEXT, 2, true, null, 0, false, false)
        ));
        TableImpl table = new TableImpl(schema);

        RowId rowId = table.insert(new RecordImpl(schema, 7, 1, "first"));
        table.insert(new RecordImpl(schema, 7, 2, "second"));
        table.insert(new RecordImpl(schema, 8, 1, "third"));

        assertEquals(rowId, table.findRowId(Key.of(7, 1)));
        assertEquals("second", first(table, Key.of(7, 2)).get("value"));
        assertEquals("third", first(table, Key.of(8, 1)).get("value"));
    }

    @Test
    void requiresPrimaryKeyInSchema() {
        Schema schema = new SchemaImpl(List.of(
                new Column("id", ColumnType.INT, 0, true, null, 0, false, false)
        ));

        assertThrows(IllegalArgumentException.class, () -> new TableImpl(schema));
    }
}
