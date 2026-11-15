package org.bdshka.server.protocol.server;

import java.util.List;

import org.bdshka.engine.QueryResult;
import org.bdshka.server.protocol.MessageWriter;
import org.bdshka.server.protocol.Protocol;
import org.bdshka.server.protocol.ServerMessage;

public final class RowDescription implements ServerMessage {

    private static final int TEXT_FORMAT = 0;

    private final List<QueryResult.ResultColumn> columns;

    public RowDescription(List<QueryResult.ResultColumn> columns) {
        this.columns = List.copyOf(columns);
    }

    @Override
    public char type() {
        return Protocol.MSG_ROW_DESCRIPTION;
    }

    @Override
    public void write(MessageWriter.Body body) {
        body.int16(columns.size());
        for (QueryResult.ResultColumn column : columns) {
            body.cstring(column.name())
                    .int32(0)
                    .int16(0)
                    .int32(column.pgOid())
                    .int16(typeLength(column.pgOid()))
                    .int32(-1)
                    .int16(TEXT_FORMAT);
        }
    }

    public static int typeLength(int pgOid) {
        return switch (pgOid) {
            case 16 -> 1;
            case 21 -> 2;
            case 23, 700, 1082 -> 4;
            case 20, 701, 1083, 1114 -> 8;
            default -> -1;
        };
    }
}