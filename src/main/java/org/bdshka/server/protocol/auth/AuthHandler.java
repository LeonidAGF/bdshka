package org.bdshka.server.protocol.auth;

public interface AuthHandler {

    AuthMethod method();
    byte[] salt();
    boolean verify(String user, byte[] proof);
}