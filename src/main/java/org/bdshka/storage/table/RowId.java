package org.bdshka.storage.table;

// Идентифицирует строку по странице, на которой она находится, и слоту на этой странице.
public record RowId(long pageId, int slot) implements Comparable<RowId> {

    public RowId {
        if (pageId < 0) {
            throw new IllegalArgumentException("Page id must not be negative");
        }
        if (slot < 0) {
            throw new IllegalArgumentException("Slot must not be negative");
        }
    }

    @Override
    public int compareTo(RowId other) {
        int pageComparison = Long.compare(pageId, other.pageId);
        return pageComparison != 0 ? pageComparison : Integer.compare(slot, other.slot);
    }
}
