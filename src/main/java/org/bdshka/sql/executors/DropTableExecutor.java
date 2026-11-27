package org.bdshka.sql.executors;

import java.util.Locale;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.drop.Drop;
import org.bdshka.base.DbException;
import org.bdshka.base.SqlStatus;
import org.bdshka.engine.QueryResult;
import org.bdshka.sql.StatementExecutor;
import org.bdshka.storage.Storage;

public final class DropTableExecutor implements StatementExecutor {

    private final Storage storage;

    public DropTableExecutor(Storage storage) {
        this.storage = storage;
    }

    @Override
    public boolean supports(Statement statement) {
        return statement instanceof Drop drop && "TABLE".equalsIgnoreCase(drop.getType());
    }

    @Override
    public QueryResult execute(Statement statement) {
        if (!supports(statement)) {
            throw new DbException(SqlStatus.FEATURE_NOT_SUPPORTED, "Expected DROP TABLE");
        }

        Drop drop = (Drop) statement;
        String tableName = validate(drop);
        storage.dropTable(tableName, drop.isIfExists());
        return new QueryResult.Command("DROP TABLE");
    }

    private static String validate(Drop drop) {
        var options = drop.getParameters();
        if (drop.isUsingTemporary() || (options != null && !options.isEmpty())) {
            throw new DbException(SqlStatus.FEATURE_NOT_SUPPORTED, "Unsupported DROP TABLE option");
        }

        String name = drop.getName().getFullyQualifiedName();
        if (!name.matches("[A-Za-z_][A-Za-z_0-9$]*")) {
            throw new DbException(SqlStatus.FEATURE_NOT_SUPPORTED, "Expected a simple unquoted table name");
        }
        return name.toLowerCase(Locale.ROOT);
    }
}
