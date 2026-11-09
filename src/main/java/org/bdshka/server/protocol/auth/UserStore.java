package org.bdshka.server.protocol.auth;

import java.util.Optional;

public interface UserStore {
    Optional<String> passwordOf(String user);
}