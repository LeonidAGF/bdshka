package org.bdshka.server.protocol.server;

import org.bdshka.server.protocol.MessageWriter;
import org.bdshka.server.protocol.Protocol;
import org.bdshka.server.protocol.ServerMessage;

public final class AuthenticationOk implements ServerMessage {

    @Override
    public char type() {
        return Protocol.MSG_AUTH;
    }

    @Override
    public void write(MessageWriter.Body body) {
        body.int32(Protocol.AUTH_OK);
    }
}