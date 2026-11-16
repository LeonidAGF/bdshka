package org.bdshka.server.auth;

public final class TrustAuth implements AuthMechanism {

    @Override
    public int statusCode() {
        return 0;
    }

    @Override
    public boolean verify(String user, String secret, byte[] md5Salt) {
        return true;
    }
}