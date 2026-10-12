package bdshka.server.protocol;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;

import static broker.network.Protocol.FRAME_SIZE_BYTES;

/**
 * Reads a Kafka-compatible request from a socket channel.
 *
 * Kafka wire protocol:
 * [messageSize: int32]
 * [apiKey: int16]
 * [apiVersion: int16]
 * [correlationId: int32]
 * [clientId: nullable_string]
 * [body: bytes...]
 *
 *
 * {@code messageSize} is the size of everything that follows it (header + body).
 */
public class MessageReader {

    /**
     * Reads a single Kafka request from the given socket channel.
     *
     * @param socket the socket channel to read from
     * @return the decoded packet, or {@code null} if the connection was closed
     * @throws IOException if an I/O error occurs
     */
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

    /**
     * Reads the 4-byte message size from the socket.
     *
     * @return message size, or -1 if connection was closed
     */
    private int readMessageSize(SocketChannel socket) throws IOException {
        ByteBuffer sizeBuffer = ByteBuffer.allocate(FRAME_SIZE_BYTES);
        sizeBuffer.order(ByteOrder.BIG_ENDIAN);

        if (!readFully(socket, sizeBuffer)) {
            return -1;
        }
        sizeBuffer.flip();
        return sizeBuffer.getInt();
    }

    /**
     * Reads bytes into the buffer until it is full or the connection is closed.
     *
     * @return true if the buffer is full, false if connection was closed before filling
     */
    private boolean readFully(SocketChannel socket, ByteBuffer buffer) throws IOException {
        while (buffer.hasRemaining()) {
            int read = socket.read(buffer);
            if (read < 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Reads a Kafka nullable string from the buffer.
     *
     * <p>Format: 2-byte signed length. If length is negative, the string is null.
     * Otherwise, read exactly {@code length} bytes as UTF-8.
     *
     * <p>Note: this handles non-flexible (pre-KIP-482) format. Flexible versions
     * use compact strings (varint length), which are not supported in this MVP reader.
     *
     * @param buffer the buffer to read from
     * @return the decoded string, or {@code null}
     */
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