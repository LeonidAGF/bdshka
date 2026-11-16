package org.bdshka.server;

import java.util.Map;
import java.util.Objects;

import org.bdshka.engine.EngineSession;

public final class ClientSession {

    private final EngineSession engineSession;
    private final String user;
    private final String database;
    private final Map<String, String> startupParameters;
    private final int processId;
    private final int secretKey;

    public ClientSession(EngineSession engineSession, String user, String database,
                         Map<String, String> startupParameters, int processId, int secretKey) {
        this.engineSession = Objects.requireNonNull(engineSession, "engineSession");
        this.user = Objects.requireNonNull(user, "user");
        this.database = Objects.requireNonNull(database, "database");
        this.startupParameters = Map.copyOf(startupParameters);
        this.processId = processId;
        this.secretKey = secretKey;
    }

    public EngineSession engineSession() {
        return engineSession;
    }

    public String user() {
        return user;
    }

    public String database() {
        return database;
    }

    public Map<String, String> startupParameters() {
        return startupParameters;
    }

    public int processId() {
        return processId;
    }

    public int secretKey() {
        return secretKey;
    }
}