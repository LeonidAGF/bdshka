package org.bdshka.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.bdshka.storage.table.Column;
import org.bdshka.storage.table.ColumnType;
import org.bdshka.storage.table.Key;
import org.bdshka.storage.table.RowId;
import org.bdshka.storage.table.Table;
import org.junit.jupiter.api.Test;

class MemoryStorageTest {

    private static Schema schema() {
        return new SchemaImpl(List.of(
                new Column("id", ColumnType.INT, 0, false, null, 0, true, true),
                new Column("name", ColumnType.VARCHAR, 1, false, null, 32, false, false)
        ));
    }

    @Test
    void createsAndFindsTable() {
        MemoryStorage storage = new MemoryStorage();
        Schema schema = schema();

        Table table = storage.createTable("users", schema);

        assertEquals("users", table.name());
        assertSame(table, storage.getTable("users"));
        assertEquals(Optional.of(table), storage.findTable("users"));
        assertEquals(Optional.empty(), storage.findTable("missing"));
        assertTrue(storage.tableNames().contains("users"));
    }

    @Test
    void rejectsDuplicateAndMissingTable() {
        MemoryStorage storage = new MemoryStorage();
        storage.createTable("users", schema());

        assertThrows(IllegalArgumentException.class,
                () -> storage.createTable("users", schema()));
        assertThrows(IllegalArgumentException.class, () -> storage.getTable("missing"));
        assertThrows(IllegalArgumentException.class, () -> storage.dropTable("missing", false));
    }

    @Test
    void dropTableRemovesIt() {
        MemoryStorage storage = new MemoryStorage();
        storage.createTable("users", schema());

        storage.dropTable("users", false);

        assertFalse(storage.tableNames().contains("users"));
        assertThrows(IllegalArgumentException.class, () -> storage.getTable("users"));
    }

    @Test
    void dropTableWithIfExistsDoesNotThrow() {
        MemoryStorage storage = new MemoryStorage();

        storage.dropTable("missing", true);

        assertFalse(storage.tableNames().contains("missing"));
    }

    @Test
    void worksWithTableOperations() {
        MemoryStorage storage = new MemoryStorage();
        Schema schema = schema();
        Table table = storage.createTable("users", schema);

        RowId rowId = table.insert(new RecordImpl(schema, 1, "alice"));

        assertEquals(rowId, table.findRowId(Key.of(1)));
    }
}
