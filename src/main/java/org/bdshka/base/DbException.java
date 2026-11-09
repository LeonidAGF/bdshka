package org.bdshka.base;

public class DbException extends RuntimeException {

    private final String sqlState;

    public DbException(String sqlState, String message) {
        super(message);
        this.sqlState = sqlState;
    }

    public DbException(String sqlState, String message, Throwable cause) {
        super(message, cause);
        this.sqlState = sqlState;
    }

    public String sqlState() {
        return sqlState;
    }
}