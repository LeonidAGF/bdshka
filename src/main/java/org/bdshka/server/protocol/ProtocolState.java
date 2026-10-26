package org.bdshka.server.protocol;

public enum ProtocolState {
    STARTUP,         // ждём StartupMessage (без байта типа)
    AUTHENTICATING,  // ждём PasswordMessage
    READY,           // ждём Query / Parse / Bind / ...
    ERROR,           // ошибка — ждём Sync или Terminate
    TERMINATED       // соединение закрыто
}