package org.bdshka.storage.table;

import java.util.NoSuchElementException;

import org.bdshka.storage.Cursor;
import org.bdshka.storage.Record;

// Курсор по нулю или одной строке, используется при поиске по первичному ключу.
final class SingleRecordCursor implements Cursor {

    private Record record;
    private boolean closed;

    SingleRecordCursor(Record record) {
        this.record = record;
    }

    @Override
    public boolean hasNext() {
        return !closed && record != null;
    }

    @Override
    public Record next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        Record result = record;
        record = null;
        return result;
    }

    @Override
    public void close() {
        closed = true;
        record = null;
    }
}
