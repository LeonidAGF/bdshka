package org.bdshka.sql;

import java.math.BigDecimal;
import net.sf.jsqlparser.expression.BooleanValue;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.SignedExpression;
import net.sf.jsqlparser.expression.StringValue;
import org.bdshka.base.DbException;
import org.bdshka.base.SqlStatus;

public final class ExpressionToJavaValueMapper {

    private ExpressionToJavaValueMapper() {}

    public static Object map(Expression expression) {
        return switch (expression) {
            case LongValue number -> number.getBigIntegerValue();
            case DoubleValue number -> new BigDecimal(number.toString());
            case SignedExpression signed -> {
                if (!(signed.getExpression() instanceof LongValue || signed.getExpression() instanceof DoubleValue)) {
                    throw new DbException(SqlStatus.FEATURE_NOT_SUPPORTED, "Expected a signed numeric literal");
                }
                yield new BigDecimal(signed.toString());
            }
            case StringValue text -> text.getNotExcapedValue();
            case BooleanValue bool -> bool.getValue();
            case NullValue ignored -> null;
            default -> throw new DbException(SqlStatus.FEATURE_NOT_SUPPORTED, "Expected a literal: " + expression);
        };
    }
}
