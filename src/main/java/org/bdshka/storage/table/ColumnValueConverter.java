package org.bdshka.storage.table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;

public final class ColumnValueConverter {

    private ColumnValueConverter() {}

    public static Object convert(Column column, Object value) {
        if (value == null) {
            return null;
        }
        ColumnType type = column.getType();
        return switch (type) {
            case SMALLINT -> toSmallInt(column, value);
            case INT -> toInteger(column, value);
            case BIGINT -> toLong(column, value);
            case DOUBLE -> toDouble(column, value);
            case REAL -> toFloat(column, value);
            case DECIMAL -> toDecimal(column, value);
            case BOOLEAN -> toBoolean(column, value);
            case VARCHAR, CHAR, TEXT -> toText(column, value);
            case DATE -> toDate(column, value);
            case TIME -> toTime(column, value);
            case TIMESTAMP -> toTimestamp(column, value);
            case BYTEA -> toBytes(column, value);
        };
    }

    private static Short toSmallInt(Column column, Object value) {
        long number = toLongNumber(column, value);
        if (number < Short.MIN_VALUE || number > Short.MAX_VALUE) {
            throw typeError(column, value);
        }
        return (short) number;
    }

    private static Integer toInteger(Column column, Object value) {
        long number = toLongNumber(column, value);
        if (number < Integer.MIN_VALUE || number > Integer.MAX_VALUE) {
            throw typeError(column, value);
        }
        return (int) number;
    }

    private static Long toLong(Column column, Object value) {
        return toLongNumber(column, value);
    }

    private static long toLongNumber(Column column, Object value) {
        if (value instanceof Number number) {
            double asDouble = number.doubleValue();
            long asLong = number.longValue();
            if (asDouble == (double) asLong) {
                return asLong;
            }
        } else if (value instanceof String text) {
            try {
                return Long.parseLong(text);
            } catch (NumberFormatException ignored) {
                // обрабатывается ниже
            }
        }
        throw typeError(column, value);
    }

    private static Double toDouble(Column column, Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value instanceof String text) {
            try {
                return Double.parseDouble(text);
            } catch (NumberFormatException ignored) {
                // обрабатывается ниже
            }
        }
        throw typeError(column, value);
    }

    private static Float toFloat(Column column, Object value) {
        if (value instanceof Number number) {
            return number.floatValue();
        }
        if (value instanceof String text) {
            try {
                return Float.parseFloat(text);
            } catch (NumberFormatException ignored) {
                // обрабатывается ниже
            }
        }
        throw typeError(column, value);
    }

    private static BigDecimal toDecimal(Column column, Object value) {
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        if (value instanceof Number number) {
            return new BigDecimal(number.toString());
        }
        if (value instanceof String text) {
            try {
                return new BigDecimal(text);
            } catch (NumberFormatException ignored) {
                // обрабатывается ниже
            }
        }
        throw typeError(column, value);
    }

    private static Boolean toBoolean(Column column, Object value) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value instanceof String text) {
            if ("true".equalsIgnoreCase(text)) {
                return true;
            }
            if ("false".equalsIgnoreCase(text)) {
                return false;
            }
        }
        throw typeError(column, value);
    }

    private static String toText(Column column, Object value) {
        if (!(value instanceof CharSequence text)) {
            throw typeError(column, value);
        }
        return text.toString();
    }

    private static LocalDate toDate(Column column, Object value) {
        if (value instanceof LocalDate date) {
            return date;
        }
        if (value instanceof java.sql.Date date) {
            return date.toLocalDate();
        }
        if (value instanceof String text) {
            try {
                return LocalDate.parse(text);
            } catch (java.time.format.DateTimeParseException ignored) {
                // обрабатывается ниже
            }
        }
        throw typeError(column, value);
    }

    private static LocalTime toTime(Column column, Object value) {
        if (value instanceof LocalTime time) {
            return time;
        }
        if (value instanceof java.sql.Time time) {
            return time.toLocalTime();
        }
        if (value instanceof String text) {
            try {
                return LocalTime.parse(text);
            } catch (java.time.format.DateTimeParseException ignored) {
                // обрабатывается ниже
            }
        }
        throw typeError(column, value);
    }

    private static LocalDateTime toTimestamp(Column column, Object value) {
        if (value instanceof LocalDateTime timestamp) {
            return timestamp;
        }
        if (value instanceof java.sql.Timestamp timestamp) {
            return timestamp.toLocalDateTime();
        }
        if (value instanceof Instant instant) {
            return LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
        }
        if (value instanceof LocalDate date) {
            return date.atStartOfDay();
        }
        if (value instanceof String text) {
            try {
                return LocalDateTime.parse(text);
            } catch (java.time.format.DateTimeParseException ignored) {
                // ниже пробуем разобрать как Instant
            }
            try {
                return LocalDateTime.ofInstant(Instant.parse(text), ZoneOffset.UTC);
            } catch (java.time.format.DateTimeParseException ignored) {
                // обрабатывается ниже
            }
        }
        throw typeError(column, value);
    }

    private static byte[] toBytes(Column column, Object value) {
        if (value instanceof byte[] bytes) {
            return bytes.clone();
        }
        throw typeError(column, value);
    }

    private static IllegalArgumentException typeError(Column column, Object value) {
        return new IllegalArgumentException(
                "Value " + value + " is not valid for column " + column.getName()
                        + " of type " + column.getType());
    }

}
