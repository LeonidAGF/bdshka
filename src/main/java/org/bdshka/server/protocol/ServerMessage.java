package org.bdshka.server.protocol;

public interface ServerMessage {
    char type();
    void write(MessageWriter.Body body);
}