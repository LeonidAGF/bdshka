package org.bdshka.server;


import org.bdshka.dispatcher.Dispatcher;
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
    private final Dispatcher dispatcher;

    public Server(int port,Dispatcher dispatcher) throws IOException {
        serverSocket = ServerSocketChannel.open();
        serverSocket.bind(new InetSocketAddress(port));
        this.port = port;
        this.dispatcher = dispatcher;
    }

    public void start() throws Exception{
        log.info("Started");
        while (serverSocket.isOpen()) {
            SocketChannel client = serverSocket.accept();
            log.info("Client connected: "+client.getRemoteAddress());
            executor.submit(new ConnectionHandler(client,dispatcher));
        }
    }

    public void stop() throws IOException {

        executor.shutdown();
        serverSocket.close();

    }

}
