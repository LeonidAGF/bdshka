package org.bdshka.sql.executors;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import net.sf.jsqlparser.expression.operators.relational.ParenthesedExpressionList;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Values;
import org.bdshka.base.DbException;
import org.bdshka.base.SqlStatus;
import org.bdshka.engine.QueryResult;
import org.bdshka.sql.ExpressionToJavaValueMapper;
import org.bdshka.sql.StatementExecutor;
import org.bdshka.storage.RecordImpl;
import org.bdshka.storage.Schema;
import org.bdshka.storage.Storage;
import org.bdshka.storage.table.Column;
import org.bdshka.storage.table.Table;

public final class InsertExecutor implements StatementExecutor {

    private final Storage storage;

    public InsertExecutor(Storage storage) {
        this.storage = storage;
    }

    @Override
    public boolean supports(Statement statement) {
        return statement instanceof Insert;
    }

    @Override
    public QueryResult execute(Statement statement) {
        if (!(statement instanceof Insert insert)) {
            throw new DbException(SqlStatus.FEATURE_NOT_SUPPORTED, "Expected INSERT");
        }
        ValidatedInsert validated = validate(insert);
        Schema schema = validated.schema();
        var row = validated.row();
        var targetColumns = insert.getColumns();

        Object[] data = schema.getColumns().stream().map(Column::getDefaultValue).toArray();
        Set<Integer> assignedColumns = new HashSet<>();

        for (int i = 0; i < row.size(); i++) {
            int position = targetColumns == null ? i
                    : resolveColumnPosition(schema, targetColumns.get(i).getColumnName(), assignedColumns);
            data[position] = ExpressionToJavaValueMapper.map(row.get(i));
        }

        validated.table().insert(new RecordImpl(schema, data));
        return new QueryResult.Command("INSERT 0 1");
    }

    private static int resolveColumnPosition(Schema schema, String name, Set<Integer> assignedColumns) {
        int position;
        try {
            position = schema.getColumnIndex(name);
        } catch (IllegalArgumentException e) {
            throw new DbException(SqlStatus.UNDEFINED_COLUMN, "Unknown column: " + name, e);
        }
        if (!assignedColumns.add(position)) {
            throw new DbException("42701", "Column specified twice: " + name);
        }
        return position;
    }

    private record ValidatedInsert(Table table, Schema schema, ParenthesedExpressionList<?> row) {}

    private ValidatedInsert validate(Insert insert) {
        if (!(insert.getSelect() instanceof Values values)
                || !(values.getExpressions() instanceof ParenthesedExpressionList<?> row)
                || insert.getWithItemsList() != null
                || insert.getConflictAction() != null
                || insert.getReturningClause() != null) {
            throw new DbException(SqlStatus.FEATURE_NOT_SUPPORTED, "Expected a single-row INSERT ... VALUES");
        }
        String tableName = insert.getTable().getFullyQualifiedName().toLowerCase(Locale.ROOT);
        Table table = storage.getTable(tableName);
        Schema schema = table.getSchema();
        var targetColumns = insert.getColumns();
        int targetCount = targetColumns == null ? schema.getColumns().size() : targetColumns.size();
        if (row.size() != targetCount) {
            throw new DbException(SqlStatus.SYNTAX_ERROR, "INSERT column count does not match value count");
        }
        return new ValidatedInsert(table, schema, row);
    }

}
