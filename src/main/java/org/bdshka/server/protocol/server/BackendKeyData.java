package org.bdshka.server.protocol.server;

import org.bdshka.server.protocol.MessageWriter;
import org.bdshka.server.protocol.Protocol;
import org.bdshka.server.protocol.ServerMessage;

public final class BackendKeyData implements ServerMessage {

    private final int processId;
    private final int secretKey;

    public BackendKeyData(int processId, int secretKey) {
        this.processId = processId;
        this.secretKey = secretKey;
    }

    @Override
    public char type() {
        return Protocol.MSG_BACKEND_KEY_DATA;
    }

    @Override
    public void write(MessageWriter.Body body) {
        body.int32(processId).int32(secretKey);
    }
}