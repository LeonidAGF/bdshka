package org.bdshka.sql;

import java.util.List;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import org.bdshka.base.DbException;
import org.bdshka.base.SqlStatus;

public final class SqlParserImpl implements SqlParser {

    @Override
    public List<Statement> parse(String sql) {
        if (sql.isBlank()) {
            return List.of();
        }

        try {
            return CCJSqlParserUtil.parseStatements(sql);
        } catch (JSQLParserException e) {
            throw new DbException(SqlStatus.SYNTAX_ERROR, e.getMessage(), e);
        }
    }
}
