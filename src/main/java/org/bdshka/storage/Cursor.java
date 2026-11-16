package org.bdshka.storage;

public interface Cursor extends AutoCloseable {

    boolean hasNext();

    Record next();

    @Override
    void close();
}