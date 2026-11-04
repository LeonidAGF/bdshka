package org.bdshka.dispatcher;

import java.util.List;

public interface QueryEngine {
    QueryResult execute(String sql);

    record QueryResult(List<String> columns, List<List<String>> rows, String tag) {}

    class PgException extends RuntimeException {
        public final String sqlState;
        public PgException(String sqlState, String message) { super(message); this.sqlState = sqlState; }
    }

}