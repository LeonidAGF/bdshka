package org.bdshka.server.protocol;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;

public class MessageReader {

    public Message read(SocketChannel socket) throws IOException {

        int messageSize = readMessageSize(socket);

        if (messageSize < 0) {
            return null;
        }

        ByteBuffer messageBuffer = ByteBuffer.allocate(messageSize);
        messageBuffer.order(ByteOrder.BIG_ENDIAN);

        if (!readFully(socket, messageBuffer)) {
            return null;
        }
        messageBuffer.flip();

        short apiKey = messageBuffer.getShort();
        short apiVersion = messageBuffer.getShort();
        int correlationId = messageBuffer.getInt();
        String clientId = readNullableString(messageBuffer);

        byte[] payload = new byte[messageBuffer.remaining()];
        messageBuffer.get(payload);

        return new Message(apiKey, apiVersion, correlationId, clientId, payload);
    }

    private int readMessageSize(SocketChannel socket) throws IOException {
        ByteBuffer sizeBuffer = ByteBuffer.allocate(FRAME_SIZE_BYTES);
        sizeBuffer.order(ByteOrder.BIG_ENDIAN);

        if (!readFully(socket, sizeBuffer)) {
            return -1;
        }
        sizeBuffer.flip();
        return sizeBuffer.getInt();
    }

    private boolean readFully(SocketChannel socket, ByteBuffer buffer) throws IOException {
        while (buffer.hasRemaining()) {
            if (socket.read(buffer) < 0) {
                return false;
            }
        }
        return true;
    }


}