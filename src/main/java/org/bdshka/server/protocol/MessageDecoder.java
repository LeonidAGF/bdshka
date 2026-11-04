package org.bdshka.server.protocol;

import org.bdshka.server.protocol.messages.frontend.*;

public class MessageDecoder {

    public FrontendMessage decode(Message envelope) throws ProtocolException {
        if (envelope.getType() == 0) {
            return StartupMessage.fromPayload(envelope.getPayload());
        }
        return switch (envelope.getType()) {
            case Protocol.MSG_QUERY -> QueryMessage.fromPayload(envelope.getPayload());
            case Protocol.MSG_PARSE -> ParseMessage.fromPayload(envelope.getPayload());
            case Protocol.MSG_BIND -> BindMessage.fromPayload(envelope.getPayload());
            case Protocol.MSG_EXECUTE -> ExecuteMessage.fromPayload(envelope.getPayload());
            case Protocol.MSG_SYNC -> SyncMessage.fromPayload(envelope.getPayload());
            case Protocol.MSG_TERMINATE -> TerminateMessage.fromPayload(envelope.getPayload());
            case Protocol.MSG_PASSWORD -> PasswordMessage.fromPayload(envelope.getPayload());
            default -> throw new ProtocolException(
                    "Unknown message type: " + (char) envelope.getType());
        };
    }
}