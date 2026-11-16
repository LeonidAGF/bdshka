package org.bdshka.server.protocol.client;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.bdshka.base.DbException;
import org.bdshka.server.protocol.ClientMessage;
import org.bdshka.server.protocol.Protocol;

public final class StartupMessage implements ClientMessage {

    private final String user;
    private final String database;
    private final Map<String, String> parameters;

    private StartupMessage(String user, String database, Map<String, String> parameters) {
        this.user = user;
        this.database = database;
        this.parameters = Map.copyOf(parameters);
    }

    public static StartupMessage fromPayload(byte[] payload) {
        ByteBuffer buffer = ByteBuffer.wrap(payload);
        int version = buffer.getInt();
        if (version != Protocol.PROTOCOL_VERSION) {
            throw new DbException(Protocol.PROTOCOL_VIOLATION, "Unsupported protocol version: " + version);
        }
        byte[] rest = new byte[buffer.remaining()];
        buffer.get(rest);
        String[] parts = new String(rest, StandardCharsets.UTF_8).split("\0", -1);
        Map<String, String> params = new HashMap<>();
        for (int i = 0; i + 1 < parts.length; i += 2) {
            if (parts[i].isEmpty()) {
                break;
            }
            params.put(parts[i], parts[i + 1]);
        }
        String user = params.get("user");
        if (user == null || user.isBlank()) {
            throw new DbException(Protocol.PROTOCOL_VIOLATION, "StartupMessage must contain user");
        }
        return new StartupMessage(user, params.getOrDefault("database", user), params);
    }

    @Override
    public byte type() {
        return 0;
    }

    public String user() {
        return user;
    }

    public String database() {
        return database;
    }

    public Map<String, String> parameters() {
        return parameters;
    }
}