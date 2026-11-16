package org.bdshka.sql;

import java.util.List;
import net.sf.jsqlparser.statement.Statement;

public interface SqlParser {

    /**
     * Разбирает пакет команд.
     * Бросает DbException(SYNTAX_ERROR), если текст не разобрался.
     * Служебные команды (BEGIN, SET, SHOW) сюда не передаются, их обрабатывает engine.
     */
    List<Statement> parse(String sql);
}