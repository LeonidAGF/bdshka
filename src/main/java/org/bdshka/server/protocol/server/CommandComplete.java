package org.bdshka.server.protocol.server;

import java.util.Objects;

import org.bdshka.server.protocol.MessageWriter;
import org.bdshka.server.protocol.Protocol;
import org.bdshka.server.protocol.ServerMessage;

public final class CommandComplete implements ServerMessage {

    private final String tag;

    public CommandComplete(String tag) {
        this.tag = Objects.requireNonNull(tag, "tag");
    }

    @Override
    public char type() {
        return Protocol.MSG_COMMAND_COMPLETE;
    }

    @Override
    public void write(MessageWriter.Body body) {
        body.cstring(tag);
    }
}