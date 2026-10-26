package org.bdshka.server.protocol;

import org.bdshka.server.protocol.messages.server.sMessage;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;

public class MessageWriter {

    public void write(SocketChannel channel, sMessage message) throws IOException {
        byte[] payload = message.getPayload();
        int length = payload.length + 4;

        ByteBuffer buffer = ByteBuffer.allocate(1 + 4 + payload.length)
                .order(ByteOrder.BIG_ENDIAN);
        buffer.put(message.getType());
        buffer.putInt(length);
        buffer.put(payload);
        buffer.flip();

        while (buffer.hasRemaining()) {
            if (channel.write(buffer) < 0) {
                throw new IOException("Channel closed");
            }
        }
    }
}