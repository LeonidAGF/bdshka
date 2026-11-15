package org.bdshka.server.protocol.client;

import org.bdshka.server.protocol.ClientMessage;
import org.bdshka.server.protocol.Protocol;

public final class TerminateMessage implements ClientMessage {

    @Override
    public byte type() {
        return Protocol.MSG_TERMINATE;
    }
}