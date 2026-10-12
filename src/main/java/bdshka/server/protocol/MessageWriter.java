package bdshka.server.protocol;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;

/**
 * Writes a Kafka-compatible response to a socket channel.
 *
 * <p>Kafka response wire format:
 * <pre>
 * [messageSize: int32]  — size of everything that follows
 * [correlationId: int32]
 * [body: bytes...]
 * </pre>
 *
 * <p>This writer supports non-flexible response format. For flexible versions
 * (KIP-482) the response header may contain tagged fields.
 */
public class MessageWriter {

    private static final int MESSAGE_SIZE_BYTES = 4;
    private static final int CORRELATION_ID_BYTES = 4;

    /**
     * Writes a Kafka response to the given socket channel.
     *
     * @param channel the socket channel to write to
     * @param packet  the response packet; only {@code correlationId} and {@code payload} are used
     * @throws IOException if an I/O error occurs
     */
    public void write(SocketChannel channel, Message packet) throws IOException {
        byte[] body = packet.getPayload();

        int messageSize = CORRELATION_ID_BYTES + body.length;

        ByteBuffer buffer = ByteBuffer.allocate(MESSAGE_SIZE_BYTES + messageSize);
        buffer.order(ByteOrder.BIG_ENDIAN);

        buffer.putInt(messageSize);
        buffer.putInt(packet.getCorrelationId());
        buffer.put(body);

        buffer.flip();
        while (buffer.hasRemaining()) {
            int written = channel.write(buffer);
            if (written < 0) {
                throw new IOException("Channel closed");
            }
        }
    }
}