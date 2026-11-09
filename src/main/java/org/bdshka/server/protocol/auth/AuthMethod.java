package org.bdshka.server.protocol.auth;

public enum AuthMethod {
    TRUST(-1),
    CLEARTEXT(3),
    MD5(5);

    public final int requestCode;

    AuthMethod(int requestCode) {
        this.requestCode = requestCode;
    }
}