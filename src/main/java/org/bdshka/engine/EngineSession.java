package org.bdshka.engine;

import java.util.List;

public interface EngineSession extends AutoCloseable {
    List<QueryResult> execute(String sql);
    List<QueryResult> executeExtended(String sql, List<Object> params);
    char transactionStatus();
    void close();
}