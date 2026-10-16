package org.bdshka.storage.table;


public enum ColumnType {

    INT,
    BIGINT,
    SMALLINT,
    DOUBLE,
    REAL,
    DECIMAL,
    BOOLEAN,
    VARCHAR,
    CHAR,
    TEXT,
    DATE,
    TIME,
    TIMESTAMP,
    BYTEA;

    public boolean isVariableLength() {
        return this == VARCHAR || this == CHAR;
    }
}