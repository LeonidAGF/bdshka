package org.bdshka.engine;

import java.util.List;
import org.bdshka.sql.SqlParser;
import org.bdshka.sql.StatementExecutor;

public final class SqlEngineImpl implements SqlEngine {

    private final SqlParser parser;
    private final List<StatementExecutor> executors;

    public SqlEngineImpl(SqlParser parser, List<StatementExecutor> executors) {
        this.parser = parser;
        this.executors = List.copyOf(executors);
    }

    @Override
    public EngineSession openSession() {
        return new EngineSessionImpl(parser, executors);
    }
}
