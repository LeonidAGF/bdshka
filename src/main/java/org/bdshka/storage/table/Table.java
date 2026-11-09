package org.bdshka.storage.table;

import org.bdshka.storage.Cursor;
import org.bdshka.storage.Record;
import org.bdshka.storage.RowCursor;
import org.bdshka.storage.Schema;

/** Одна таблица. Создаётся через Storage.createTable. */
public interface Table {

    String name();

    Schema getSchema();

    /** Обход всех строк. Для UPDATE/DELETE используйте, собирая RowId в список до изменений. */
    RowCursor scan();

    /** Поиск по первичному ключу. Пустой курсор, если строки нет. */
    Cursor lookup(Key key);

    /** RowId строки с этим ключом или null. */
    RowId findRowId(Key key);

    /**
     * Вставляет строку. Проверяет NOT NULL и уникальность PK.
     * DbException(NOT_NULL_VIOLATION) или DbException(UNIQUE_VIOLATION).
     */
    RowId insert(Record data);

    /** Заменяет строку. Если меняется ключ, проверяется уникальность. */
    void update(RowId rowId, Record data);

    /** Удаляет строку. Пустая операция, если строки уже нет. */
    void delete(RowId rowId);
}