package org.bdshka.storage.table;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

// Значение первичного ключа, в том числе составного; компоненты идут по схеме.
public final class Key implements Comparable<Key> {

    private final List<Object> values;

    private Key(Object[] values) {
        this.values = List.of(values);
    }

    public static Key of(Object... values) {
        Objects.requireNonNull(values, "values");
        if (values.length == 0) {
            throw new IllegalArgumentException("Key must contain at least one value");
        }
        for (Object value : values) {
            Objects.requireNonNull(value, "Key component must not be null");
        }
        return new Key(values.clone());
    }

    public List<Object> getValues() {
        return values;
    }

    @Override
    public int compareTo(Key other) {
        Objects.requireNonNull(other, "other");
        int common = Math.min(values.size(), other.values.size());
        for (int i = 0; i < common; i++) {
            int result = compareValues(values.get(i), other.values.get(i));
            if (result != 0) {
                return result;
            }
        }
        return Integer.compare(values.size(), other.values.size());
    }

    private static int compareValues(Object left, Object right) {
        if (left instanceof Number leftNumber && right instanceof Number rightNumber) {
            return compareNumbers(leftNumber, rightNumber);
        }
        if (left.getClass() == right.getClass() && left instanceof Comparable<?> comparable) {
            @SuppressWarnings("unchecked")
            Comparable<Object> typed = (Comparable<Object>) comparable;
            return typed.compareTo(right);
        }
        throw new IllegalArgumentException(
                "Cannot compare key components " + left.getClass().getName()
                        + " and " + right.getClass().getName());
    }

    private static int compareNumbers(Number left, Number right) {
        if (isFloatingPoint(left) || isFloatingPoint(right)) {
            return Double.compare(left.doubleValue(), right.doubleValue());
        }
        return new BigDecimal(left.toString()).compareTo(new BigDecimal(right.toString()));
    }

    private static boolean isFloatingPoint(Number number) {
        return number instanceof Float || number instanceof Double;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Key key)) {
            return false;
        }
        return values.equals(key.values);
    }

    @Override
    public int hashCode() {
        return values.hashCode();
    }

    @Override
    public String toString() {
        return "Key" + Arrays.toString(values.toArray());
    }
}
