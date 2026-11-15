package org.bdshka.server.protocol.server;

import org.bdshka.server.protocol.MessageWriter;
import org.bdshka.server.protocol.Protocol;
import org.bdshka.server.protocol.ServerMessage;

public final class ReadyForQuery implements ServerMessage {

    private final char transactionStatus;

    public ReadyForQuery(char transactionStatus) {
        this.transactionStatus = transactionStatus;
    }

    @Override
    public char type() {
        return Protocol.MSG_READY_FOR_QUERY;
    }

    @Override
    public void write(MessageWriter.Body body) {
        body.byte1(transactionStatus);
    }
}