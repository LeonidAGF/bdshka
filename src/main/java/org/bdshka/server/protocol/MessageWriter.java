package org.bdshka.server.protocol;

import java.io.*;
import java.nio.charset.StandardCharsets;

public final class MessageWriter {
    private final DataOutputStream out;

    public MessageWriter(OutputStream os) {
        this.out = new DataOutputStream(new BufferedOutputStream(os));
    }

    public static final class Body {
        private final ByteArrayOutputStream bos = new ByteArrayOutputStream();

        public Body int32(int v) { bos.write(v >>> 24); bos.write(v >>> 16); bos.write(v >>> 8); bos.write(v); return this; }
        public Body int16(int v) { bos.write(v >>> 8); bos.write(v); return this; }
        public Body byte1(int v) { bos.write(v); return this; }
        public Body bytes(byte[] b) { bos.writeBytes(b); return this; }
        public Body cstring(String s) { bos.writeBytes(s.getBytes(StandardCharsets.UTF_8)); bos.write(0); return this; }
    }

    public static Body body() { return new Body(); }

    public void send(char type, Body b) throws IOException {
        out.writeByte(type);
        out.writeInt(b.bos.size() + 4);
        b.bos.writeTo(out);
    }

    public void raw(int b) throws IOException { out.writeByte(b); }

    public void flush() throws IOException { out.flush(); }
}