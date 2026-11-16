package org.bdshka.server.protocol.client;

import java.nio.charset.StandardCharsets;

import org.bdshka.server.protocol.ClientMessage;
import org.bdshka.server.protocol.Protocol;

public final class PasswordMessage implements ClientMessage {

    private final String secret;

    public PasswordMessage(String secret) {
        this.secret = secret;
    }

    public static PasswordMessage fromPayload(byte[] payload) {
        String text = new String(payload, StandardCharsets.UTF_8);
        int nullByte = text.indexOf('\0');
        return new PasswordMessage(nullByte >= 0 ? text.substring(0, nullByte) : text);
    }

    @Override
    public byte type() {
        return Protocol.MSG_PASSWORD;
    }

    public String secret() {
        return secret;
    }
}