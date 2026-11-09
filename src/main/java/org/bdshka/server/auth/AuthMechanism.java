package org.bdshka.server.auth;

public interface AuthMechanism {
    int statusCode();// 0-ok 3 - cleartext 5-md5

    /**
     * Проверить ответ клиента (пароль или md5-хеш с солью).
     */
    boolean verify(String user, String secret, byte[] md5Salt);
}
