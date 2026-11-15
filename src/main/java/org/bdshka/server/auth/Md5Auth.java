package org.bdshka.server.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class Md5Auth implements AuthMechanism {

    @Override
    public int statusCode() {
        return 5;
    }

    @Override
    public boolean verify(String user, String secret, byte[] md5Salt) {
        if (secret == null || md5Salt == null) {
            return false;
        }
        String inner = md5Hex((AuthMethod.password() + user)
                .getBytes(StandardCharsets.UTF_8));
        byte[] innerBytes = inner.getBytes(StandardCharsets.UTF_8);
        byte[] combined = new byte[innerBytes.length + md5Salt.length];
        System.arraycopy(innerBytes, 0, combined, 0, innerBytes.length);
        System.arraycopy(md5Salt, 0, combined, innerBytes.length, md5Salt.length);
        return ("md5" + md5Hex(combined)).equals(secret);
    }

    static String md5Hex(byte[] data) {
        try {
            byte[] hash = MessageDigest.getInstance("MD5").digest(data);
            StringBuilder text = new StringBuilder();
            for (byte b : hash) {
                text.append(String.format("%02x", b));
            }
            return text.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("MD5 is always available in Java", exception);
        }
    }
}