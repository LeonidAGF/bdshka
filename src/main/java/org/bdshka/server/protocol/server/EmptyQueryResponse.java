package org.bdshka.server.protocol.server;

import org.bdshka.server.protocol.MessageWriter;
import org.bdshka.server.protocol.Protocol;
import org.bdshka.server.protocol.ServerMessage;

public final class EmptyQueryResponse implements ServerMessage {

    @Override
    public char type() {
        return Protocol.MSG_EMPTY_QUERY;
    }

    @Override
    public void write(MessageWriter.Body body) {

    }
}