package org.bdshka.server.protocol.server;

import java.util.Objects;

import org.bdshka.server.protocol.MessageWriter;
import org.bdshka.server.protocol.Protocol;
import org.bdshka.server.protocol.ServerMessage;

public final class ParameterStatus implements ServerMessage {

    private final String name;
    private final String value;

    public ParameterStatus(String name, String value) {
        this.name = Objects.requireNonNull(name, "name");
        this.value = Objects.requireNonNull(value, "value");
    }

    @Override
    public char type() {
        return Protocol.MSG_PARAMETER_STATUS;
    }

    @Override
    public void write(MessageWriter.Body body) {
        body.cstring(name).cstring(value);
    }
}