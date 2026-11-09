package org.bdshka.storage.table;

public enum ColumnType {

    SMALLINT(21), INT(23), BIGINT(20), REAL(700), DOUBLE(701), DECIMAL(1700),
    BOOLEAN(16), CHAR(1042), VARCHAR(1043), TEXT(25),
    DATE(1082), TIME(1083), TIMESTAMP(1114), BYTEA(17);

    private final int pgOid;

    ColumnType(int pgOid) {
        this.pgOid = pgOid;
    }

    /** OID типа в PostgreSQL. Нужен для RowDescription. */
    public int pgOid() {
        return pgOid;
    }

    public boolean isVariableLength() {
        return this == VARCHAR || this == CHAR;
    }
}