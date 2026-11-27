package org.bdshka.engine;

import java.util.ArrayList;
import java.util.List;
import net.sf.jsqlparser.statement.Statement;
import org.bdshka.base.DbException;
import org.bdshka.base.SqlStatus;
import org.bdshka.sql.SqlParser;
import org.bdshka.sql.StatementExecutor;

public final class EngineSessionImpl implements EngineSession {

    private final SqlParser parser;
    private final List<StatementExecutor> executors;
    private boolean closed;

    public EngineSessionImpl(SqlParser parser, List<StatementExecutor> executors) {
        this.parser = parser;
        this.executors = List.copyOf(executors);
    }

    @Override
    public List<QueryResult> execute(String sql) {
        ensureOpen();

        List<QueryResult> results = new ArrayList<>();
        try {
            List<Statement> statements = parser.parse(sql);
            if (statements.isEmpty()) {
                return List.of(new QueryResult.Empty());
            }

            for (Statement statement : statements) {
                QueryResult result = executeStatement(statement);
                results.add(result);
                if (result instanceof QueryResult.ErrorResult) {
                    break;
                }
            }
        } catch (DbException e) {
            results.add(QueryResult.error(e));
        }

        return results;
    }

    private QueryResult executeStatement(Statement statement) {
        for (StatementExecutor executor : executors) {
            if (executor.supports(statement)) {
                return executor.execute(statement);
            }
        }

        throw new DbException(SqlStatus.FEATURE_NOT_SUPPORTED,
                "Unsupported statement: " + statement.getClass().getSimpleName());
    }

    @Override
    public List<QueryResult> executeExtended(String sql, List<Object> params) {
        ensureOpen();
        return List.of(new QueryResult.ErrorResult(
                SqlStatus.FEATURE_NOT_SUPPORTED, "Extended queries are not supported yet"));
    }

    @Override
    public char transactionStatus() {
        return 'I';
    }

    @Override
    public void close() {
        closed = true;
    }

    private void ensureOpen() {
        if (closed) {
            throw new IllegalStateException("Session is closed");
        }
    }
}
