package org.bdshka.server.protocol;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;


public class MessageWriter {

    private static final int MESSAGE_SIZE_BYTES = 4;
    private static final int CORRELATION_ID_BYTES = 4;

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