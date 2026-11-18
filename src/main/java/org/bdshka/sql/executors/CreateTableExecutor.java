package org.bdshka.sql.executors;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import org.bdshka.base.DbException;
import org.bdshka.base.SqlStatus;
import org.bdshka.engine.QueryResult;
import org.bdshka.sql.StatementExecutor;
import org.bdshka.sql.TypeMapper;
import org.bdshka.storage.SchemaImpl;
import org.bdshka.storage.Storage;
import org.bdshka.storage.table.Column;
import org.bdshka.storage.table.ColumnType;

public final class CreateTableExecutor implements StatementExecutor {

    private static final Pattern TYPE = Pattern.compile("([A-Za-z][A-Za-z0-9 ]*?)(?:\\s*\\(\\s*(\\d+)\\s*\\))?");
    private static final Pattern IDENTIFIER = Pattern.compile("[A-Za-z_][A-Za-z_0-9$]*");

    private final Storage storage;
    private final TypeMapper typeMapper;

    public CreateTableExecutor(Storage storage, TypeMapper typeMapper) {
        this.storage = storage;
        this.typeMapper = typeMapper;
    }

    @Override
    public boolean supports(Statement statement) {
        return statement instanceof CreateTable;
    }

    @Override
    public QueryResult execute(Statement statement) {
        if (!(statement instanceof CreateTable create)) {
            throw unsupported("Expected CREATE TABLE");
        }
        validate(create);

        String tableName = identifier(create.getTable().getFullyQualifiedName());
        List<Column> columns = new ArrayList<>();

        for (ColumnDefinition definition : create.getColumnDefinitions()) {
            columns.add(toColumn(definition, columns.size()));
        }

        storage.createTable(tableName, new SchemaImpl(columns));
        return new QueryResult.Command("CREATE TABLE");
    }

    private static void validate(CreateTable create) {
        List<ColumnDefinition> definitions = create.getColumnDefinitions();
        if (definitions == null || definitions.isEmpty()
                || create.isIfNotExists() || create.isOrReplace() || create.isUnlogged()
                || create.getSelect() != null || create.getLikeTable() != null
                || create.getIndexes() != null || create.getColumns() != null
                || create.getCreateOptionsStrings() != null || create.getTableOptionsStrings() != null
                || create.getRowMovement() != null || create.getSpannerInterleaveIn() != null) {
            throw unsupported("Expected CREATE TABLE with a simple column list");
        }
    }

    private Column toColumn(ColumnDefinition definition, int position) {
        String name = identifier(definition.getColumnName());
        String declaration = definition.getColDataType().toString().strip();
        Matcher matcher = TYPE.matcher(declaration);
        if (!matcher.matches()) {
            throw unsupported("Unsupported column type declaration: " + declaration);
        }

        String typeName = matcher.group(1).strip();
        Integer parameter = null;

        if (matcher.group(2) != null) {
            try {
                parameter = Integer.valueOf(matcher.group(2));
            } catch (NumberFormatException e) {
                throw new DbException("22023", "Type parameter is too large: " + declaration, e);
            }
        }

        ColumnType type = typeMapper.toColumnType(typeName, parameter);
        int maxLength = switch (type) {
            case CHAR -> parameter == null ? 1 : parameter;
            case VARCHAR -> parameter == null ? 0 : parameter;
            default -> 0;
        };

        List<String> specs = definition.getColumnSpecs();
        String constraint = specs == null ? "" : String.join(" ", specs).toUpperCase(Locale.ROOT);
        boolean primaryKey = constraint.equals("PRIMARY KEY");
        boolean nullable = constraint.isEmpty() || constraint.equals("NULL");

        if (!primaryKey && !nullable && !constraint.equals("NOT NULL")) {
            throw unsupported("Unsupported constraint on " + name + ": " + constraint);
        }
        return new Column(name, type, position, nullable, null, maxLength, primaryKey, primaryKey);
    }

    private static String identifier(String name) {
        if (!IDENTIFIER.matcher(name).matches()) {
            throw unsupported("Expected a simple unquoted identifier: " + name);
        }

        return name.toLowerCase(Locale.ROOT);
    }

    private static DbException unsupported(String message) {
        return new DbException(SqlStatus.FEATURE_NOT_SUPPORTED, message);
    }
}
