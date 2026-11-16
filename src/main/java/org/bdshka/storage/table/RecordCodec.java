package org.bdshka.storage.table;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.bdshka.storage.Record;
import org.bdshka.storage.RecordImpl;
import org.bdshka.storage.Schema;
import org.bdshka.storage.SchemaImpl;

// Проверяет, сериализует и десериализует строки одной таблицы.
final class RecordCodec {

    private static final int NULL_FLAG = 0;
    private static final int VALUE_FLAG = 1;

    private final Schema schema;
    private final List<Column> columns;
    private final List<Integer> primaryKeyIndexes;

    RecordCodec(Schema schema) {
        this.schema = Objects.requireNonNull(schema, "schema");
        this.columns = schema.getColumns();
        this.primaryKeyIndexes = findPrimaryKeyIndexes(schema);
    }

    Object[] normalize(Record record) {
        Objects.requireNonNull(record, "record");
        Object[] values = new Object[columns.size()];
        for (int i = 0; i < columns.size(); i++) {
            values[i] = normalizeValue(columns.get(i), record.get(i));
        }
        return values;
    }

    Key keyOf(Object[] values) {
        if (primaryKeyIndexes.isEmpty()) {
            throw new IllegalStateException("Table has no primary key");
        }
        Object[] keyValues = new Object[primaryKeyIndexes.size()];
        for (int i = 0; i < primaryKeyIndexes.size(); i++) {
            keyValues[i] = values[primaryKeyIndexes.get(i)];
        }
        return Key.of(keyValues);
    }

    byte[] encode(Object[] values) {
        Objects.requireNonNull(values, "values");
        if (values.length != columns.size()) {
            throw new IllegalArgumentException("Value count does not match schema");
        }

        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            try (DataOutputStream output = new DataOutputStream(bytes)) {
                for (int i = 0; i < columns.size(); i++) {
                    Object value = values[i];
                    if (value == null) {
                        output.writeByte(NULL_FLAG);
                    } else {
                        output.writeByte(VALUE_FLAG);
                        writeValue(output, columns.get(i).getType(), value);
                    }
                }
            }
            return bytes.toByteArray();
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    Record decode(byte[] data, int offset, int length) {
        return new RecordImpl(schema, decodeValues(data, offset, length));
    }

    Object[] decodeValues(byte[] data, int offset, int length) {
        try (DataInputStream input =
                     new DataInputStream(new ByteArrayInputStream(data, offset, length))) {
            Object[] values = new Object[columns.size()];
            for (int i = 0; i < columns.size(); i++) {
                int flag = input.readUnsignedByte();
                values[i] = flag == NULL_FLAG
                        ? null
                        : readValue(input, columns.get(i).getType());
            }
            return values;
        } catch (IOException exception) {
            throw new IllegalArgumentException("Corrupted row data", exception);
        }
    }

    private static List<Integer> findPrimaryKeyIndexes(Schema schema) {
        if (schema instanceof SchemaImpl schemaImpl) {
            return schemaImpl.getPrimaryKeyIndexes();
        }

        List<Integer> indexes = new ArrayList<>();
        for (int i = 0; i < schema.getColumns().size(); i++) {
            if (schema.getColumns().get(i).isPrimaryKey()) {
                indexes.add(i);
            }
        }
        return List.copyOf(indexes);
    }

    private Object normalizeValue(Column column, Object value) {
        if (value == null) {
            if (!column.isNullable() || column.isPrimaryKey()) {
                Object defaultValue = column.getDefaultValue();
                if (defaultValue == null) {
                    throw new IllegalArgumentException(
                            "Column " + column.getName() + " must not be null");
                }
                value = defaultValue;
            } else {
                return null;
            }
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
        String result = text.toString();
        if (column.getMaxLength() > 0 && result.length() > column.getMaxLength()) {
            throw new IllegalArgumentException(
                    "Value for column " + column.getName() + " exceeds max length "
                            + column.getMaxLength());
        }
        return result;
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

    private static void writeValue(DataOutputStream output, ColumnType type, Object value)
            throws IOException {
        switch (type) {
            case SMALLINT -> output.writeShort((Short) value);
            case INT -> output.writeInt((Integer) value);
            case BIGINT -> output.writeLong((Long) value);
            case DOUBLE -> output.writeDouble((Double) value);
            case REAL -> output.writeFloat((Float) value);
            case DECIMAL -> writeString(output, value.toString());
            case BOOLEAN -> output.writeBoolean((Boolean) value);
            case VARCHAR, CHAR, TEXT -> writeString(output, (String) value);
            case DATE -> output.writeLong(((LocalDate) value).toEpochDay());
            case TIME -> output.writeLong(((LocalTime) value).toNanoOfDay());
            case TIMESTAMP -> {
                LocalDateTime timestamp = (LocalDateTime) value;
                output.writeLong(timestamp.toLocalDate().toEpochDay());
                output.writeLong(timestamp.toLocalTime().toNanoOfDay());
            }
            case BYTEA -> {
                byte[] bytes = (byte[]) value;
                output.writeInt(bytes.length);
                output.write(bytes);
            }
            default -> throw new IllegalStateException("Unsupported type: " + type);
        }
    }

    private static Object readValue(DataInputStream input, ColumnType type) throws IOException {
        return switch (type) {
            case SMALLINT -> input.readShort();
            case INT -> input.readInt();
            case BIGINT -> input.readLong();
            case DOUBLE -> input.readDouble();
            case REAL -> input.readFloat();
            case DECIMAL -> new BigDecimal(readString(input));
            case BOOLEAN -> input.readBoolean();
            case VARCHAR, CHAR, TEXT -> readString(input);
            case DATE -> LocalDate.ofEpochDay(input.readLong());
            case TIME -> LocalTime.ofNanoOfDay(input.readLong());
            case TIMESTAMP -> LocalDateTime.of(
                    LocalDate.ofEpochDay(input.readLong()),
                    LocalTime.ofNanoOfDay(input.readLong()));
            case BYTEA -> {
                int length = input.readInt();
                if (length < 0) {
                    throw new IOException("Invalid byte array length: " + length);
                }
                byte[] bytes = new byte[length];
                input.readFully(bytes);
                yield bytes;
            }
        };
    }

    private static void writeString(DataOutputStream output, String value) throws IOException {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        output.writeInt(bytes.length);
        output.write(bytes);
    }

    private static String readString(DataInputStream input) throws IOException {
        int length = input.readInt();
        if (length < 0) {
            throw new IOException("Invalid string length: " + length);
        }
        byte[] bytes = new byte[length];
        input.readFully(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
