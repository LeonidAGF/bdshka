package org.bdshka.server.protocol;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 *
 */
public final class Protocol {
    public static final char MSG_QUERY = 'Q';
    public static final char MSG_PARSE = 'P';
    public static final char MSG_BIND = 'B';
    public static final char MSG_EXECUTE = 'E';
    public static final char MSG_SYNC = 'S';
    public static final char MSG_TERMINATE = 'X';
    public static final char MSG_AUTH = 'R';
    public static final char MSG_READY_FOR_QUERY = 'Z';
    public static final char MSG_DATA_ROW = 'D';
    public static final char MSG_ROW_DESC = 'T';
    public static final char MSG_ERROR = 'E';
}