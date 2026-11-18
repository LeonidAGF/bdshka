package org.bdshka.storage.table;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

        Object converted = ColumnValueConverter.convert(column, value);
        if (converted instanceof String text && column.getMaxLength() > 0 && text.length() > column.getMaxLength()) {
            throw new IllegalArgumentException(
                    "Value for column " + column.getName() + " exceeds max length "
                            + column.getMaxLength());
        }
        return converted;
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
