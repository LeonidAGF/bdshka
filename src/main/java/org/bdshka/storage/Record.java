package org.bdshka.storage;

public interface Record {
    Object get(int columnIndex);
    Object get(String columnName);
}
