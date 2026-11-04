package org.bdshka.storage.table;

import org.bdshka.storage.Cursor;
import org.bdshka.storage.Record;
import org.bdshka.storage.Schema;

// Таблица хранит строки на страницах, которые индексируются B-деревом.
public interface Table {

    Schema getSchema();

    // Сканирует все строки таблицы. Строки читаются страница за страницей.
    Cursor scan();

    // Находит одну строку по первичному ключу.
    Cursor lookup(Key key);

    // Возвращает физическое расположение строки или null, если строки нет.
    RowId findRowId(Key key);

    // Вставляет строку и возвращает её физическое расположение.
    RowId insert(Record data);

    void update(RowId rowId, Record data);

    void delete(RowId rowId);
}
