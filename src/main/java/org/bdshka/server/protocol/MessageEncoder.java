package org.bdshka.server.protocol;

import org.bdshka.server.protocol.messages.backend.BackendMessage;

public class MessageEncoder {
    public byte[] encode(BackendMessage message) {
        return message.getPayload();
    }
}