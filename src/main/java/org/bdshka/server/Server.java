package org.bdshka.server;

import org.bdshka.engine.SqlEngine;
import org.bdshka.server.auth.AuthMechanism;
import org.bdshka.server.handlers.ConnectionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private static final Logger log = LoggerFactory.getLogger(Server.class);
    private final ServerSocketChannel serverSocket;
    private final int port;
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private final SqlEngine engine;
    private final AuthMechanism auth;

    public Server(int port, SqlEngine engine, AuthMechanism auth) throws IOException {
        serverSocket = ServerSocketChannel.open();
        serverSocket.bind(new InetSocketAddress(port));
        this.port = port;
        this.engine = engine;
        this.auth = auth;
    }

    public void start() throws Exception {
        log.info("bdshka listening on port " + port);
        while (serverSocket.isOpen()) {
            SocketChannel client = serverSocket.accept();
            log.info("Client connected: " + client.getRemoteAddress());
            executor.submit(new ConnectionHandler(client, engine, auth));
        }
    }

    public void stop() throws IOException {
        executor.shutdown();
        serverSocket.close();
    }
}