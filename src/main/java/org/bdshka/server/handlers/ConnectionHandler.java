package org.bdshka.server.handlers;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.SecureRandom;
import java.util.List;

import org.bdshka.base.DbException;
import org.bdshka.base.SqlStatus;
import org.bdshka.engine.EngineSession;
import org.bdshka.engine.QueryResult;
import org.bdshka.engine.SqlEngine;
import org.bdshka.server.ClientSession;
import org.bdshka.server.auth.AuthMechanism;
import org.bdshka.server.protocol.Message;
import org.bdshka.server.protocol.MessageReader;
import org.bdshka.server.protocol.MessageWriter;
import org.bdshka.server.protocol.Protocol;
import org.bdshka.server.protocol.client.PasswordMessage;
import org.bdshka.server.protocol.client.QueryMessage;
import org.bdshka.server.protocol.client.StartupMessage;
import org.bdshka.server.protocol.server.AuthenticationOk;
import org.bdshka.server.protocol.server.AuthenticationRequest;
import org.bdshka.server.protocol.server.BackendKeyData;
import org.bdshka.server.protocol.server.ErrorResponse;
import org.bdshka.server.protocol.server.ParameterStatus;
import org.bdshka.server.protocol.server.ReadyForQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ConnectionHandler implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(ConnectionHandler.class);
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int SALT_LENGTH = 4;

    private final SocketChannel socket;
    private final SqlEngine engine;
    private final AuthMechanism auth;
    private final MessageReader reader = new MessageReader();
    private final QueryResultEncoder encoder = new QueryResultEncoder();

    public ConnectionHandler(SocketChannel socket, SqlEngine engine, AuthMechanism auth) {
        this.socket = socket;
        this.engine = engine;
        this.auth = auth;
    }

    @Override
    public void run() {
        try (socket) {
            handleConnection();
        } catch (DbException exception) {
            log.error("Protocol error: {}", exception.getMessage());
        } catch (IOException exception) {
            log.error("Connection error", exception);
        } finally {
            log.info("Client disconnected");
        }
    }

    private void handleConnection() throws IOException {
        MessageWriter writer = new MessageWriter(socket);
        Message startup = reader.readStartup(socket);
        if (startup == null) {
            throw new DbException(Protocol.PROTOCOL_VIOLATION, "No startup message received");
        }

        StartupMessage request = StartupMessage.fromPayload(startup.getPayload());
        log.info("Startup: user={}, database={}", request.user(), request.database());

        ClientSession session = authenticate(writer, request);
        if (session == null) {
            return;
        }
        sendWelcome(writer, session);

        try (EngineSession engineSession = session.engineSession()) {
            commandLoop(writer, engineSession);
        }
    }

    private ClientSession authenticate(MessageWriter writer, StartupMessage request)
            throws IOException {
        if (auth.statusCode() == Protocol.AUTH_OK) {
            writer.send(new AuthenticationOk());
            writer.flush();
            return newSession(writer, request);
        }
        byte[] salt = new byte[SALT_LENGTH];
        RANDOM.nextBytes(salt);
        writer.send(new AuthenticationRequest(auth.statusCode(), salt));
        writer.flush();

        Message response = reader.readNormal(socket);
        if (response == null || response.getType() != Protocol.MSG_PASSWORD) {
            writer.send(ErrorResponse.fatal(Protocol.PROTOCOL_VIOLATION,
                    "Expected password message"));
            writer.flush();
            return null;
        }
        PasswordMessage password = PasswordMessage.fromPayload(response.getPayload());
        if (!auth.verify(request.user(), password.secret(), salt)) {
            writer.send(ErrorResponse.fatal(Protocol.INVALID_PASSWORD,
                    "password authentication failed for user \"" + request.user() + "\""));
            writer.flush();
            return null;
        }
        writer.send(new AuthenticationOk());
        writer.flush();
        return newSession(writer, request);
    }

    private ClientSession newSession(MessageWriter writer, StartupMessage request) {
        return new ClientSession(engine.openSession(), request.user(), request.database(),
                request.parameters(), RANDOM.nextInt(), RANDOM.nextInt());
    }

    private void sendWelcome(MessageWriter writer, ClientSession session) throws IOException {
        writer.send(new ParameterStatus("server_version", "16.0 (bdshka 0.1)"));
        writer.send(new ParameterStatus("server_encoding", "UTF8"));
        writer.send(new ParameterStatus("client_encoding", "UTF8"));
        writer.send(new ParameterStatus("DateStyle", "ISO, MDY"));
        writer.send(new ParameterStatus("integer_datetimes", "on"));
        writer.send(new ParameterStatus("standard_conforming_strings", "on"));
        writer.send(new BackendKeyData(session.processId(), session.secretKey()));
        writer.send(new ReadyForQuery((char) Protocol.TX_IDLE));
        writer.flush();
    }

    private void commandLoop(MessageWriter writer, EngineSession engineSession)
            throws IOException {
        while (true) {
            Message packet = reader.readNormal(socket);
            if (packet == null) {
                return;
            }
            switch ((char) packet.getType()) {
                case Protocol.MSG_QUERY -> {
                    QueryMessage query = QueryMessage.fromPayload(packet.getPayload());
                    handleQuery(writer, engineSession, query.sql());
                }
                case Protocol.MSG_TERMINATE -> {
                    return;
                }
                default -> {
                    log.info("Unsupported message type '{}'", (char) packet.getType());
                    writer.send(ErrorResponse.of(SqlStatus.FEATURE_NOT_SUPPORTED,
                            "Message type '" + (char) packet.getType()
                                    + "' is not supported yet"));
                    writer.send(new ReadyForQuery(engineSession.transactionStatus()));
                }
            }
            writer.flush();
        }
    }

    private void handleQuery(MessageWriter writer, EngineSession engineSession, String sql)
            throws IOException {
        try {
            List<QueryResult> results = sql.isBlank()
                    ? List.of(new QueryResult.Empty())
                    : engineSession.execute(sql);
            for (QueryResult result : results) {
                encoder.encode(writer, result);
            }
        } catch (DbException exception) {
            writer.send(ErrorResponse.of(exception.sqlState(), exception.getMessage()));
        } catch (RuntimeException exception) {
            log.error("Query failed", exception);
            writer.send(ErrorResponse.of(SqlStatus.INTERNAL_ERROR, exception.toString()));
        } finally {
            writer.send(new ReadyForQuery(engineSession.transactionStatus()));
        }
    }

    private static int firstInt32(Message message) {
        byte[] payload = message.getPayload();
        if (payload == null || payload.length < 4) {
            return -1;
        }
        return ByteBuffer.wrap(payload).getInt();
    }
}