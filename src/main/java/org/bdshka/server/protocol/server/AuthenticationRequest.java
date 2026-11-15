package org.bdshka.server.protocol.server;

import java.util.Objects;

import org.bdshka.server.protocol.MessageWriter;
import org.bdshka.server.protocol.Protocol;
import org.bdshka.server.protocol.ServerMessage;

public final class AuthenticationRequest implements ServerMessage {

    private final int code;
    private final byte[] md5Salt;

    public AuthenticationRequest(int code) {
        this(code, null);
    }

    public AuthenticationRequest(int code, byte[] md5Salt) {
        this.code = code;
        this.md5Salt = md5Salt;
    }

    @Override
    public char type() {
        return Protocol.MSG_AUTH;
    }

    @Override
    public void write(MessageWriter.Body body) {
        body.int32(code);
        if (code == Protocol.AUTH_MD5_PASSWORD) {
            body.bytes(Objects.requireNonNull(md5Salt, "md5Salt"));
        }
    }
}