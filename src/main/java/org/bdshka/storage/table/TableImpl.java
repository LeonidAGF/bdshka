package org.bdshka.storage.table;

import java.util.Comparator;
import java.util.Objects;

import org.bdshka.storage.Cursor;
import org.bdshka.storage.Record;
import org.bdshka.storage.Schema;
import org.bdshka.storage.btree.BTree;

// Таблица на страницах фиксированного размера с индексами в B-деревьях.
public final class TableImpl implements Table {

    public static final int DEFAULT_PAGE_SIZE = 8192;

    private static final int BTREE_DEGREE = 8;

    private final Schema schema;
    private final RecordCodec codec;
    private final int pageSize;
    private final BTree<Long, Page> pages;
    private final BTree<Key, RowId> primaryIndex;

    private long nextPageId = 1;
    private long rowCount;
    private Page lastPage;

    public TableImpl(Schema schema) {
        this(schema, DEFAULT_PAGE_SIZE);
    }

    public TableImpl(Schema schema, int pageSize) {
        this.schema = Objects.requireNonNull(schema, "schema");
        if (pageSize < Page.MIN_PAGE_SIZE) {
            throw new IllegalArgumentException("Page size is too small: " + pageSize);
        }
        if (schema.getColumns().stream().noneMatch(Column::isPrimaryKey)) {
            throw new IllegalArgumentException("Table schema must define a primary key");
        }

        this.pageSize = pageSize;
        this.codec = new RecordCodec(schema);
        this.pages = new BTree<>(BTREE_DEGREE, Comparator.comparingLong(Long::longValue));
        this.primaryIndex = new BTree<>(BTREE_DEGREE, Comparator.naturalOrder());
    }

    @Override
    public Schema getSchema() {
        return schema;
    }

    @Override
    public Cursor scan() {
        return new TableCursor(pages.valueIterator(), codec);
    }

    @Override
    public Cursor lookup(Key key) {
        Objects.requireNonNull(key, "key");
        RowId rowId = primaryIndex.get(key);
        if (rowId == null) {
            return new SingleRecordCursor(null);
        }

        Page page = pages.get(rowId.pageId());
        if (page == null) {
            return new SingleRecordCursor(null);
        }

        byte[] row = page.read(rowId.slot());
        Record record = row == null ? null : codec.decode(row, 0, row.length);
        return new SingleRecordCursor(record);
    }

    @Override
    public RowId findRowId(Key key) {
        Objects.requireNonNull(key, "key");
        return primaryIndex.get(key);
    }

    @Override
    public RowId insert(Record data) {
        Objects.requireNonNull(data, "data");
        Object[] values = codec.normalize(data);
        Key key = codec.keyOf(values);
        if (primaryIndex.containsKey(key)) {
            throw new IllegalArgumentException("Duplicate primary key: " + key);
        }

        byte[] row = codec.encode(values);
        RowId rowId = appendRow(row);
        primaryIndex.put(key, rowId);
        rowCount++;
        return rowId;
    }

    @Override
    public void update(RowId rowId, Record data) {
        Objects.requireNonNull(rowId, "rowId");
        Objects.requireNonNull(data, "data");

        Page page = requirePage(rowId);
        byte[] oldRow = page.read(rowId.slot());
        if (oldRow == null) {
            throw new IllegalArgumentException("Row does not exist: " + rowId);
        }

        Key oldKey = codec.keyOf(codec.decodeValues(oldRow, 0, oldRow.length));
        Object[] newValues = codec.normalize(data);
        Key newKey = codec.keyOf(newValues);
        boolean keyChanged = newKey.compareTo(oldKey) != 0;

        if (keyChanged) {
            RowId existing = primaryIndex.get(newKey);
            if (existing != null && !existing.equals(rowId)) {
                throw new IllegalArgumentException("Duplicate primary key: " + newKey);
            }
        }

        byte[] newRow = codec.encode(newValues);
        page.update(rowId.slot(), newRow);

        if (keyChanged) {
            RowId indexed = primaryIndex.get(oldKey);
            if (rowId.equals(indexed)) {
                primaryIndex.remove(oldKey);
            }
            primaryIndex.put(newKey, rowId);
        }
    }

    @Override
    public void delete(RowId rowId) {
        Objects.requireNonNull(rowId, "rowId");

        Page page = pages.get(rowId.pageId());
        if (page == null) {
            return;
        }
        byte[] row = page.read(rowId.slot());
        if (row == null) {
            return;
        }

        Key key = codec.keyOf(codec.decodeValues(row, 0, row.length));
        if (page.delete(rowId.slot())) {
            RowId indexed = primaryIndex.get(key);
            if (rowId.equals(indexed)) {
                primaryIndex.remove(key);
            }
            rowCount--;
        }
    }

    // Количество строк, которые сейчас хранятся в таблице.
    public long size() {
        return rowCount;
    }

    // Количество выделенных страниц, включая страницы, все строки которых удалены.
    public int pageCount() {
        return pages.size();
    }

    private RowId appendRow(byte[] row) {
        int slot;
        Page page = lastPage;

        if (page != null) {
            slot = page.insert(row);
            if (slot >= 0) {
                return new RowId(page.id(), slot);
            }
        }

        page = new Page(nextPageId++, pageSize);
        slot = page.insert(row);
        if (slot < 0) {
            throw new IllegalArgumentException(
                    "Row does not fit into an empty page of size " + pageSize);
        }
        pages.put(page.id(), page);
        lastPage = page;
        return new RowId(page.id(), slot);
    }

    private Page requirePage(RowId rowId) {
        Page page = pages.get(rowId.pageId());
        if (page == null) {
            throw new IllegalArgumentException("Page does not exist: " + rowId.pageId());
        }
        return page;
    }
}
