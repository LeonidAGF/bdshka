package org.bdshka.storage.table;

import java.util.Arrays;
import java.util.Objects;

// Страница фиксированного размера: строки растут с конца к каталогу слотов.
final class Page {

    static final int HEADER_SIZE = Integer.BYTES;
    static final int SLOT_SIZE = 2 * Integer.BYTES;
    static final int MIN_PAGE_SIZE = HEADER_SIZE + SLOT_SIZE + 1;

    private static final int DELETED = -1;

    private final long id;
    private byte[] data;
    private int slotCount;

    Page(long id, int pageSize) {
        if (id < 0) {
            throw new IllegalArgumentException("Page id must not be negative");
        }
        if (pageSize < MIN_PAGE_SIZE) {
            throw new IllegalArgumentException("Page size is too small: " + pageSize);
        }
        this.id = id;
        this.data = new byte[pageSize];
    }

    long id() {
        return id;
    }

    int slotCount() {
        return slotCount;
    }

    int pageSize() {
        return data.length;
    }

    int maxRowLength() {
        return data.length - HEADER_SIZE - SLOT_SIZE;
    }

    // Добавляет строку и возвращает номер слота или -1, если она не помещается.
    int insert(byte[] row) {
        Objects.requireNonNull(row, "row");
        if (row.length > maxRowLength()) {
            throw new IllegalArgumentException(
                    "Row of " + row.length + " bytes does not fit into a page of "
                            + data.length + " bytes");
        }

        if (!hasRoomForNewSlot(row.length)) {
            compact();
        }
        if (!hasRoomForNewSlot(row.length)) {
            return -1;
        }

        int offset = firstRowOffset() - row.length;
        int slot = slotCount;
        slotCount++;
        writeInt(data, 0, slotCount);

        System.arraycopy(row, 0, data, offset, row.length);
        writeSlot(slot, offset, row.length);
        return slot;
    }

    byte[] read(int slot) {
        if (!isLive(slot)) {
            return null;
        }
        int offset = readInt(data, slotOffset(slot));
        int length = readInt(data, slotOffset(slot) + Integer.BYTES);
        return Arrays.copyOfRange(data, offset, offset + length);
    }

    void update(int slot, byte[] row) {
        Objects.requireNonNull(row, "row");
        if (!isLive(slot)) {
            throw new IllegalArgumentException("Row slot is empty: " + slot);
        }
        if (row.length > maxRowLength()) {
            throw new IllegalArgumentException(
                    "Row of " + row.length + " bytes does not fit into a page of "
                            + data.length + " bytes");
        }

        byte[][] rows = readAllSlots();
        rows[slot] = row.clone();
        rebuild(rows);
    }

    boolean delete(int slot) {
        if (!isLive(slot)) {
            return false;
        }
        writeInt(data, slotOffset(slot) + Integer.BYTES, DELETED);
        return true;
    }

    boolean isEmpty() {
        for (int slot = 0; slot < slotCount; slot++) {
            if (isLive(slot)) {
                return false;
            }
        }
        return true;
    }

    int liveCount() {
        int count = 0;
        for (int slot = 0; slot < slotCount; slot++) {
            if (isLive(slot)) {
                count++;
            }
        }
        return count;
    }

    int freeSpace() {
        return firstRowOffset() - slotDirectoryEnd(slotCount);
    }

    private boolean isLive(int slot) {
        return slot >= 0
                && slot < slotCount
                && readInt(data, slotOffset(slot) + Integer.BYTES) >= 0;
    }

    private boolean hasRoomForNewSlot(int rowLength) {
        return firstRowOffset() - slotDirectoryEnd(slotCount + 1) >= rowLength;
    }

    private int slotOffset(int slot) {
        return HEADER_SIZE + slot * SLOT_SIZE;
    }

    private static int slotDirectoryEnd(int slots) {
        return HEADER_SIZE + slots * SLOT_SIZE;
    }

    private int firstRowOffset() {
        int first = data.length;
        for (int slot = 0; slot < slotCount; slot++) {
            if (isLive(slot)) {
                first = Math.min(first, readInt(data, slotOffset(slot)));
            }
        }
        return first;
    }

    private byte[][] readAllSlots() {
        byte[][] rows = new byte[slotCount][];
        for (int slot = 0; slot < slotCount; slot++) {
            rows[slot] = read(slot);
        }
        return rows;
    }

    private void compact() {
        rebuild(readAllSlots());
    }

    private void rebuild(byte[][] rows) {
        long required = slotDirectoryEnd(rows.length);
        for (byte[] row : rows) {
            if (row != null) {
                required += row.length;
            }
        }
        if (required > data.length) {
            throw new IllegalArgumentException(
                    "Rows require " + required + " bytes but page size is " + data.length);
        }

        byte[] fresh = new byte[data.length];
        writeInt(fresh, 0, rows.length);

        int dataStart = fresh.length;
        for (int slot = 0; slot < rows.length; slot++) {
            int base = slotOffset(slot);
            if (rows[slot] == null) {
                writeInt(fresh, base, 0);
                writeInt(fresh, base + Integer.BYTES, DELETED);
            } else {
                dataStart -= rows[slot].length;
                System.arraycopy(rows[slot], 0, fresh, dataStart, rows[slot].length);
                writeInt(fresh, base, dataStart);
                writeInt(fresh, base + Integer.BYTES, rows[slot].length);
            }
        }
        data = fresh;
        slotCount = rows.length;
    }

    private static int readInt(byte[] bytes, int offset) {
        return ((bytes[offset] & 0xff) << 24)
                | ((bytes[offset + 1] & 0xff) << 16)
                | ((bytes[offset + 2] & 0xff) << 8)
                | (bytes[offset + 3] & 0xff);
    }

    private static void writeInt(byte[] bytes, int offset, int value) {
        bytes[offset] = (byte) (value >>> 24);
        bytes[offset + 1] = (byte) (value >>> 16);
        bytes[offset + 2] = (byte) (value >>> 8);
        bytes[offset + 3] = (byte) value;
    }

    private void writeSlot(int slot, int offset, int length) {
        int base = slotOffset(slot);
        writeInt(data, base, offset);
        writeInt(data, base + Integer.BYTES, length);
    }
}
