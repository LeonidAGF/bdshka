package bdshka.server.protocol;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 *
 */
public final class Protocol {
    public static final int FRAME_SIZE_BYTES = 4;
    public static final short API_PRODUCE = 0;
    public static final short API_FETCH = 1;
    public static final short API_LIST_OFFSETS = 2;
    public static final short API_METADATA = 3;
    public static final short API_API_VERSIONS = 18;

    public static String readString(ByteBuffer buffer) {
        short length = buffer.getShort();

        if (length < 0) {
            return null;
        }

        byte[] bytes = new byte[length];
        buffer.get(bytes);

        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static byte[] readBytes(ByteBuffer buffer) {
        int length = buffer.getInt();

        if (length < 0) {
            return null;
        }

        byte[] bytes = new byte[length];
        buffer.get(bytes);

        return bytes;
    }

    public static void writeString(ByteBuffer buffer, String value) {
        if (value == null) {
            buffer.putShort((short) -1);
            return;
        }

        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        buffer.putShort((short) bytes.length);
        buffer.put(bytes);
    }

    public static void writeBytes(ByteBuffer buffer, byte[] value) {
        if (value == null) {
            buffer.putInt(-1);
            return;
        }

        buffer.putInt(value.length);
        buffer.put(value);
    }
}
