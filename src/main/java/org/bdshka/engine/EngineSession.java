package org.bdshka.engine;

import java.util.List;

// Состояние одного клиентского соединения
public interface EngineSession extends AutoCloseable {


    List<QueryResult> execute(String sql);

    // I вне транзакции, T внутри, E транзакция сломана. Уходит в ReadyForQuery.
    char transactionStatus();
    List<QueryResult> executeExtended(String sql, List<Object> params);

    @Override
    void close();
}