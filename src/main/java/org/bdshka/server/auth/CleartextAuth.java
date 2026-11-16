package org.bdshka.server.auth;

public final class CleartextAuth implements AuthMechanism {

    @Override
    public int statusCode() {
        return 3;
    }

    @Override
    public boolean verify(String user, String secret, byte[] md5Salt) {
        return AuthMethod.password().equals(secret);
    }
}