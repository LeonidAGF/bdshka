package org.bdshka.storage.table;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.bdshka.storage.Cursor;
import org.bdshka.storage.Record;

// Лениво сканирует страницы, декодируя только одну строку за раз.
final class TableCursor implements Cursor {

    private final Iterator<Page> pages;
    private final RecordCodec codec;

    private Page page;
    private int slot;
    private Record next;
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
    public Record next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        Record result = next;
        next = null;
        return result;
    }

    private Record fetch() {
        while (true) {
            if (page != null && slot < page.slotCount()) {
                byte[] row = page.read(slot++);
                if (row != null) {
                    return codec.decode(row, 0, row.length);
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
