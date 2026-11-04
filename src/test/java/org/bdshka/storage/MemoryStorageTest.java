package org.bdshka.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.bdshka.storage.table.Column;
import org.bdshka.storage.table.ColumnType;
import org.bdshka.storage.table.Key;
import org.bdshka.storage.table.RowId;
import org.bdshka.storage.table.Table;
import org.junit.jupiter.api.Test;

class MemoryStorageTest {

    private static Schema schema() {
        return new SchemaImpl(List.of(
                new Column("users", ColumnType.INT, 0, false, null, 0, true, true),
                new Column("name", ColumnType.VARCHAR, 1, false, null, 32, false, false)
        ));
    }

    @Test
    void createsAndOpensTable() {
        MemoryStorage storage = new MemoryStorage();
        Schema schema = schema();

        storage.createTable(schema);

        assertEquals(1, storage.size());
        assertTrue(storage.containsTable("users_table"));
        Table table = storage.openTable("users_table");
        assertSame(schema, table.getSchema());
    }

    @Test
    void rejectsDuplicateAndMissing() {
        MemoryStorage storage = new MemoryStorage();
        Schema schema = schema();

        storage.createTable(schema);

        assertThrows(IllegalArgumentException.class, () -> storage.createTable(schema));
        assertThrows(IllegalArgumentException.class, () -> storage.openTable("missing"));
        assertThrows(IllegalArgumentException.class, () -> storage.dropTable("missing"));
    }

    @Test
    void dropTableRemovesIt() {
        MemoryStorage storage = new MemoryStorage();
        storage.createTable(schema());

        storage.dropTable("users_table");

        assertEquals(0, storage.size());
        assertFalse(storage.containsTable("users_table"));
        assertThrows(IllegalArgumentException.class, () -> storage.openTable("users_table"));
    }

    @Test
    void worksWithTableOperations() {
        MemoryStorage storage = new MemoryStorage();
        Schema schema = schema();
        storage.createTable(schema);

        Table table = storage.openTable("users_table");
        RowId rowId = table.insert(new RecordImpl(schema, 1, "alice"));

        assertEquals(1, ((org.bdshka.storage.table.TableImpl) table).size());
        assertEquals(rowId, ((org.bdshka.storage.table.TableImpl) table).findRowId(Key.of(1)));
    }
}
