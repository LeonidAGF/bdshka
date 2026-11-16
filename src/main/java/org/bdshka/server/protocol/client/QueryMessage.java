package org.bdshka.server.protocol.client;

import java.nio.charset.StandardCharsets;

import org.bdshka.server.protocol.ClientMessage;
import org.bdshka.server.protocol.Protocol;

public final class QueryMessage implements ClientMessage {

    private final String sql;

    public QueryMessage(String sql) {
        this.sql = sql;
    }

    public static QueryMessage fromPayload(byte[] payload) {
        String text = new String(payload, StandardCharsets.UTF_8);
        int nullByte = text.indexOf('\0');
        return new QueryMessage(nullByte >= 0 ? text.substring(0, nullByte) : text);
    }

    @Override
    public byte type() {
        return Protocol.MSG_QUERY;
    }

    public String sql() {
        return sql;
    }
}