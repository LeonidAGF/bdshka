package org.bdshka.sql.executors;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.PlainSelect;
import org.bdshka.base.DbException;
import org.bdshka.base.SqlStatus;
import org.bdshka.engine.QueryResult;
import org.bdshka.sql.ExpressionToJavaValueMapper;
import org.bdshka.sql.StatementExecutor;
import org.bdshka.storage.Record;
import org.bdshka.storage.RowCursor;
import org.bdshka.storage.Schema;
import org.bdshka.storage.Storage;
import org.bdshka.storage.table.ColumnType;
import org.bdshka.storage.table.ColumnValueConverter;
import org.bdshka.storage.table.Table;

public final class SelectExecutor implements StatementExecutor {

    private final Storage storage;

    public SelectExecutor(Storage storage) {
        this.storage = storage;
    }

    @Override
    public boolean supports(Statement statement) {
        return statement instanceof PlainSelect;
    }

    @Override
    public QueryResult execute(Statement statement) {
        if (!(statement instanceof PlainSelect select)) {
            throw unsupported("Expected SELECT");
        }
        String tableName = validate(select);
        Table table = storage.findTable(tableName)
                .orElseThrow(() -> new DbException(SqlStatus.UNDEFINED_TABLE, "Unknown table: " + tableName));
        Schema schema = table.getSchema();
        List<Integer> positions = columnPositions(select, schema);
        Predicate<Record> matches = wherePredicate(select.getWhere(), schema);

        List<QueryResult.ResultColumn> columns = new ArrayList<>();
        for (int position : positions) {
            var column = schema.getColumns().get(position);
            columns.add(new QueryResult.ResultColumn(column.getName(), column.getType().pgOid()));
        }

        List<Object[]> rows = new ArrayList<>();
        try (RowCursor cursor = table.scan()) {
            while (cursor.hasNext()) {
                Record record = cursor.next().record();
                if (!matches.test(record)) {
                    continue;
                }
                Object[] values = new Object[positions.size()];
                for (int i = 0; i < values.length; i++) {
                    values[i] = record.get(positions.get(i));
                }
                rows.add(values);
            }
        }
        return new QueryResult.Rows(columns, rows, "SELECT " + rows.size());
    }

    private static String validate(PlainSelect select) {
        if (!(select.getFromItem() instanceof net.sf.jsqlparser.schema.Table from)
                || select.getWithItemsList() != null || select.getJoins() != null || select.getLateralViews() != null) {
            throw unsupported("Expected SELECT from a single table");
        }
        if (from.getAlias() != null || from.getSampleClause() != null || from.getPivot() != null
                || from.getUnPivot() != null || from.getTimeTravel() != null || select.isUsingOnly()
                || select.getSampleClause() != null) {
            throw unsupported("Table aliases and table modifiers are not supported");
        }
        if (select.getPreWhere() != null || select.getQualify() != null) {
            throw unsupported("PREWHERE and QUALIFY are not supported");
        }
        if (select.getDistinct() != null || select.getGroupBy() != null || select.getHaving() != null
                || select.getWindowDefinitions() != null) {
            throw unsupported("DISTINCT, grouping and window clauses are not supported");
        }
        if (select.getOrderByElements() != null) {
            throw unsupported("ORDER BY is not supported");
        }
        if (select.getLimit() != null || select.getLimitBy() != null || select.getOffset() != null
                || select.getFetch() != null || select.getTop() != null || select.getFirst() != null || select.getSkip() != null) {
            throw unsupported("Row limits and offsets are not supported");
        }
        if (select.getForMode() != null || select.getForClause() != null) {
            throw unsupported("SELECT with a FOR clause is not supported");
        }
        if (select.getIntoTables() != null || select.getIntoTempTable() != null) {
            throw unsupported("SELECT INTO is not supported");
        }
        return identifier(from.getFullyQualifiedName());
    }

    private static List<Integer> columnPositions(PlainSelect select, Schema schema) {
        List<Integer> positions = new ArrayList<>();
        for (var item : select.getSelectItems()) {
            if (item.getAlias() != null) {
                throw unsupported("Column aliases are not supported");
            }
            var expression = item.getExpression();
            if (isPlainStar(expression)) {
                addAllColumnPositions(positions, schema.getColumns().size());
                continue;
            }
            positions.add(columnPosition(expression, schema));
        }
        return positions;
    }

    private static Predicate<Record> wherePredicate(Expression where, Schema schema) {
        if (where == null) {
            return record -> true;
        }
        if (!(where instanceof EqualsTo equals) || equals.getOldOracleJoinSyntax() != 0 || equals.getOraclePriorPosition() != 0) {
            throw unsupported("Expected WHERE column = literal");
        }
        int position = columnPosition(equals.getLeftExpression(), schema);
        var column = schema.getColumns().get(position);
        Object literal = ExpressionToJavaValueMapper.map(equals.getRightExpression());
        if (literal == null) {
            return record -> false;
        }
        try {
            ColumnType type = column.getType();
            if (literal instanceof Number && (type == ColumnType.SMALLINT || type == ColumnType.INT || type == ColumnType.BIGINT)) {
                BigDecimal expected = new BigDecimal(literal.toString());
                return record -> equalValues(record.get(position), expected);
            }
            Object expected = ColumnValueConverter.convert(column, literal);
            if (type == ColumnType.CHAR) {
                String text = trimPadding((String) expected);
                return record -> record.get(position) instanceof String value && text.equals(trimPadding(value));
            }
            return record -> equalValues(record.get(position), expected);
        } catch (IllegalArgumentException e) {
            throw new DbException(SqlStatus.DATATYPE_MISMATCH, e.getMessage(), e);
        }
    }

    private static boolean equalValues(Object value, Object expected) {
        if (value == null) {
            return false;
        }
        if (expected instanceof BigDecimal decimal) {
            BigDecimal actual = value instanceof BigDecimal number ? number : BigDecimal.valueOf(((Number) value).longValue());
            return decimal.compareTo(actual) == 0;
        }
        if (expected instanceof Float || expected instanceof Double) {
            return ((Number) value).doubleValue() == ((Number) expected).doubleValue() || value.equals(expected);
        }
        return expected.equals(value);
    }

    private static String trimPadding(String value) {
        int end = value.length();
        while (end > 0 && value.charAt(end - 1) == ' ') {
            end--;
        }
        return value.substring(0, end);
    }

    private static void addAllColumnPositions(List<Integer> positions, int columnCount) {
        for (int i = 0; i < columnCount; i++) {
            positions.add(i);
        }
    }

    private static boolean isPlainStar(Expression expression) {
        return expression instanceof AllColumns star && star.getClass() == AllColumns.class
                && star.getExceptColumns() == null && star.getReplaceExpressions() == null;
    }

    private static int columnPosition(Expression expression, Schema schema) {
        if (!(expression instanceof Column column) || column.getArrayConstructor() != null) {
            throw unsupported("Expected a simple column name");
        }
        String name = identifier(column.getFullyQualifiedName());
        try {
            return schema.getColumnIndex(name);
        } catch (IllegalArgumentException e) {
            throw new DbException(SqlStatus.UNDEFINED_COLUMN, "Unknown column: " + name, e);
        }
    }

    private static String identifier(String name) {
        if (!name.matches("[A-Za-z_][A-Za-z_0-9$]*")) {
            throw unsupported("Expected a simple unquoted identifier: " + name);
        }
        return name.toLowerCase(Locale.ROOT);
    }

    private static DbException unsupported(String message) {
        return new DbException(SqlStatus.FEATURE_NOT_SUPPORTED, message);
    }
}
