package org.bdshka.storage.table;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.bdshka.storage.Record;
import org.bdshka.storage.RowCursor;

// Лениво сканирует страницы, декодируя только одну строку за раз.
final class TableCursor implements RowCursor {

    private final Iterator<Page> pages;
    private final RecordCodec codec;

    private Page page;
    private int slot;
    private Entry next;
    private boolean closed;

    TableCursor(Iterator<Page> pages, RecordCodec codec) {
        this.pages = Objects.requireNonNull(pages, "pages");
        this.codec = Objects.requireNonNull(codec, "codec");
    }

    @Override
    public boolean hasNext() {
        if (closed) {
            return false;
        }
        if (next == null) {
            next = fetch();
        }
        return next != null;
    }

    @Override
    public Entry next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        Entry result = next;
        next = null;
        return result;
    }

    private Entry fetch() {
        while (true) {
            if (page != null && slot < page.slotCount()) {
                int currentSlot = slot;
                byte[] row = page.read(slot++);
                if (row != null) {
                    Record record = codec.decode(row, 0, row.length);
                    return new Entry(new RowId(page.id(), currentSlot), record);
                }
            } else {
                if (!pages.hasNext()) {
                    close();
                    return null;
                }
                page = pages.next();
                slot = 0;
            }
        }
    }

    @Override
    public void close() {
        closed = true;
        next = null;
        page = null;
    }
}
