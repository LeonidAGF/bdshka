package bdshka.server.protocol;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;

import static broker.network.Protocol.FRAME_SIZE_BYTES;


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
            int read = socket.read(buffer);
            if (read < 0) {
                return false;
            }
        }
        return true;
    }

    private String readNullableString(ByteBuffer buffer) {
        short length = buffer.getShort();
        if (length < 0) {
            return null;
        }
        byte[] bytes = new byte[length];
        buffer.get(bytes);
        return new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
    }
}