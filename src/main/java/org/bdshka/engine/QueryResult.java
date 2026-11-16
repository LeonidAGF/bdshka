package org.bdshka.engine;

import org.bdshka.base.DbException;

import java.util.List;

// Результат одной SQL команды
public sealed interface QueryResult {

    record ResultColumn(String name, int pgOid) {}

    record Rows(List<ResultColumn> columns, List<Object[]> rows, String tag) implements QueryResult {}

    record Command(String tag) implements QueryResult {}

    record Empty() implements QueryResult {}

    record ErrorResult(String sqlState, String message) implements QueryResult {}

    static QueryResult error(DbException e) {
        return new ErrorResult(e.sqlState(), e.getMessage());
    }
}