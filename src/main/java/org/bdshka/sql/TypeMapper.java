package org.bdshka.sql;

import org.bdshka.storage.table.ColumnType;

public interface TypeMapper {

    ColumnType toColumnType(String sqlTypeName, Integer length);
}