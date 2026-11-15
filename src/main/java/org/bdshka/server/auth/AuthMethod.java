package org.bdshka.server.auth;

import java.util.Locale;


public enum AuthMethod {

    TRUST, CLEARTEXT, MD5;

    public static String password() {
        return System.getenv().getOrDefault("BDSHKA_PASSWORD", "postgres");
    }

    public static AuthMethod parse(String name) {
        if (name == null || name.isBlank()) {
            return TRUST;
        }
        return switch (name.trim().toLowerCase(Locale.ROOT)) {
            case "trust" -> TRUST;
            case "cleartext", "password" -> CLEARTEXT;
            case "md5" -> MD5;
            default -> throw new IllegalArgumentException("Unknown auth method: " + name);
        };
    }

    public AuthMechanism create() {
        return switch (this) {
            case TRUST -> new TrustAuth();
            case CLEARTEXT -> new CleartextAuth();
            case MD5 -> new Md5Auth();
        };
    }
}