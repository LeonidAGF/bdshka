package org.bdshka.sql;

import org.bdshka.engine.QueryResult;
import net.sf.jsqlparser.statement.Statement;

public interface StatementExecutor {

    QueryResult execute(Statement statement);

    boolean supports(Statement statement);
}