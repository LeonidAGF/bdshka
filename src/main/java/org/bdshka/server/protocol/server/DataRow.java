package org.bdshka.server.protocol.server;

import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

import org.bdshka.server.protocol.MessageWriter;
import org.bdshka.server.protocol.Protocol;
import org.bdshka.server.protocol.ServerMessage;

public final class DataRow implements ServerMessage {

    private final Object[] values;

    public DataRow(Object[] values) {
        this.values = values.clone();
    }

    @Override
    public char type() {
        return Protocol.MSG_DATA_ROW;
    }

    @Override
    public void write(MessageWriter.Body body) {
        body.int16(values.length);
        for (Object value : values) {
            if (value == null) {
                body.int32(-1);
            } else {
                byte[] text = asText(value).getBytes(StandardCharsets.UTF_8);
                body.int32(text.length).bytes(text);
            }
        }
    }

    private static String asText(Object value) {
        if (value instanceof byte[] raw) {
            return "\\x" + HexFormat.of().formatHex(raw);
        }
        if (value instanceof Boolean bool) {
            return bool ? "t" : "f";
        }
        return String.valueOf(value);
    }
}