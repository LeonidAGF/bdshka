package org.bdshka.sql;

import java.util.Locale;
import org.bdshka.base.DbException;
import org.bdshka.base.SqlStatus;
import org.bdshka.storage.table.ColumnType;

public final class TypeMapperImpl implements TypeMapper {

    // Имена типов и псевдонимы из таблицы 8.1 PostgreSQL 18:
    // https://www.postgresql.org/docs/18/datatype.html#DATATYPE-TABLE
    @Override
    public ColumnType toColumnType(String sqlTypeName, Integer length) {
        if (sqlTypeName == null || sqlTypeName.isBlank()) {
            throw new DbException(SqlStatus.SYNTAX_ERROR, "Column type is missing");
        }

        String name = sqlTypeName.strip().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);

        if (name.equals("FLOAT")) {
            return floatType(length);
        }

        ColumnType type = switch (name) {
            case "SMALLINT", "INT2" -> ColumnType.SMALLINT;
            case "INTEGER", "INT", "INT4" -> ColumnType.INT;
            case "BIGINT", "INT8" -> ColumnType.BIGINT;
            case "REAL", "FLOAT4" -> ColumnType.REAL;
            case "DOUBLE PRECISION", "FLOAT8" -> ColumnType.DOUBLE;
            case "NUMERIC", "DECIMAL" -> ColumnType.DECIMAL;
            case "BOOLEAN", "BOOL" -> ColumnType.BOOLEAN;
            case "CHARACTER", "CHAR" -> ColumnType.CHAR;
            case "CHARACTER VARYING", "VARCHAR" -> ColumnType.VARCHAR;
            case "TEXT" -> ColumnType.TEXT;
            case "DATE" -> ColumnType.DATE;
            case "TIME", "TIME WITHOUT TIME ZONE" -> ColumnType.TIME;
            case "TIMESTAMP", "TIMESTAMP WITHOUT TIME ZONE" -> ColumnType.TIMESTAMP;
            case "BYTEA" -> ColumnType.BYTEA;
            default -> throw new DbException(SqlStatus.FEATURE_NOT_SUPPORTED, "Unsupported column type: " + sqlTypeName);
        };

        if (length != null) {
            if (!type.isVariableLength()) {
                throw new DbException(SqlStatus.FEATURE_NOT_SUPPORTED, "Unsupported type parameter: " + name + "(" + length + ")");
            }
            if (length < 1) {
                throw new DbException("22023", "Character length must be positive");
            }
        }

        return type;
    }

    // https://www.postgresql.org/docs/18/datatype-numeric.html#DATATYPE-FLOAT
    private static ColumnType floatType(Integer precision) {
        if (precision == null) {
            return ColumnType.DOUBLE;
        }
        if (precision < 1 || precision > 53) {
            throw new DbException("22023", "FLOAT precision must be between 1 and 53");
        }
        return precision <= 24 ? ColumnType.REAL : ColumnType.DOUBLE;
    }
}
