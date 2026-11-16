package org.bdshka.base;

public final class SqlStatus {

    private SqlStatus() {
    }
    public static final String SUCCESSFUL = "00000";
    public static final String SYNTAX_ERROR = "42601";
    public static final String UNDEFINED_TABLE = "42P01";
    public static final String UNDEFINED_COLUMN = "42703";
    public static final String DUPLICATE_TABLE = "42P07";
    public static final String DATATYPE_MISMATCH = "42804";
    public static final String UNIQUE_VIOLATION = "23505";
    public static final String NOT_NULL_VIOLATION = "23502";
    public static final String FEATURE_NOT_SUPPORTED = "0A000";
    public static final String INTERNAL_ERROR = "XX000";
}
