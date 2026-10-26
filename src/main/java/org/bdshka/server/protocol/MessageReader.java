package org.bdshka.server.protocol;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;

public class MessageReader {

    /** StartupMessage: [int32 length][payload], length включает себя. */
    public Message readStartup(SocketChannel channel) throws IOException {
        int length = readInt32(channel);
        if (length < 4) return null;
        byte[] payload = readBytes(channel, length - 4);
        if (payload == null) return null;
        return new Message((byte) 0, payload);
    }

    /** Обычное сообщение: [byte type][int32 length][payload]. */
    public Message readNormal(SocketChannel channel) throws IOException {
        int type = readByte(channel);
        if (type < 0) return null;
        int length = readInt32(channel);
        if (length < 4) return null;
        byte[] payload = readBytes(channel, length - 4);
        if (payload == null) return null;
        return new Message((byte) type, payload);
    }

    private int readByte(SocketChannel channel) throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(1);
        if (!readFully(channel, buf)) return -1;
        return buf.get(0) & 0xFF;
    }

    private int readInt32(SocketChannel channel) throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN);
        if (!readFully(channel, buf)) return -1;
        buf.flip();
        return buf.getInt();
    }

    private byte[] readBytes(SocketChannel channel, int n) throws IOException {
        if (n == 0) return new byte[0];
        ByteBuffer buf = ByteBuffer.allocate(n);
        if (!readFully(channel, buf)) return null;
        return buf.array();
    }

    private boolean readFully(SocketChannel channel, ByteBuffer buffer) throws IOException {
        while (buffer.hasRemaining()) {
            if (channel.read(buffer) < 0) return false;
        }
        return true;
    }
}