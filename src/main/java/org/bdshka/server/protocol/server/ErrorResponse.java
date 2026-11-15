package org.bdshka.server.protocol.server;

import java.util.Objects;

import org.bdshka.server.protocol.MessageWriter;
import org.bdshka.server.protocol.Protocol;
import org.bdshka.server.protocol.ServerMessage;

public final class ErrorResponse implements ServerMessage {

    private final String severity;
    private final String sqlState;
    private final String message;

    private ErrorResponse(String severity, String sqlState, String message) {
        this.severity = Objects.requireNonNull(severity, "severity");
        this.sqlState = Objects.requireNonNull(sqlState, "sqlState");
        this.message = Objects.requireNonNull(message, "message");
    }

    public static ErrorResponse of(String sqlState, String message) {
        return new ErrorResponse("ERROR", sqlState, message);
    }

    public static ErrorResponse fatal(String sqlState, String message) {
        return new ErrorResponse("FATAL", sqlState, message);
    }

    @Override
    public char type() {
        return Protocol.MSG_ERROR;
    }

    @Override
    public void write(MessageWriter.Body body) {
        body.byte1('S').cstring(severity)
                .byte1('C').cstring(sqlState)
                .byte1('M').cstring(message)
                .byte1(0);
    }
}