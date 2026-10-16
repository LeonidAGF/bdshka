package org.bdshka.server.handlers;


import org.bdshka.dispatcher.Dispatcher;
import org.bdshka.server.protocol.Message;
import org.bdshka.server.protocol.MessageReader;
import org.bdshka.server.protocol.MessageWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 *
 */
public class ConnectionHandler implements Runnable{

    private static final Logger log = LoggerFactory.getLogger(ConnectionHandler.class);
    private final SocketChannel socket;
    private final MessageWriter writer;
    private final MessageReader reader;
    private final Dispatcher dispatcher;

    /**
     *
     * @param socket
     */
    public ConnectionHandler(SocketChannel socket, Dispatcher dispatcher){
        this.socket = socket;
        this.dispatcher = dispatcher;
        writer = new MessageWriter();
        reader = new MessageReader();
    }

    /**
     *
     */
    @Override
    public void run() {
        try {
            while (true) {
                Message packet = reader.read(socket);
                if (packet == null) {
                    break;
                }
                log.info("Received packet. CorrelationId=" + packet.getCorrelationId());

                Message response = dispatcher.dispatch(packet);
                writer.write(socket, response);
            }
        }
        catch (IOException e) {
            log.error("Error",e);
        }
        finally {
            try {
                socket.close();
            }
            catch (IOException ignored) {
            }
            log.info("Client disconnected");
        }
    }


}
