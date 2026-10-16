package bdshka.server.protocol;

import java.util.Arrays;


public final class Message {

    private final short apiKey;
    private final short apiVersion;
    private final int correlationId;
    private final String clientId;
    private final byte[] payload;

    public Message(short apiKey, short apiVersion, int correlationId, String clientId, byte[] payload) {
        this.apiKey = apiKey;
        this.apiVersion = apiVersion;
        this.correlationId = correlationId;
        this.clientId = clientId;
        this.payload = payload == null ? new byte[0] : Arrays.copyOf(payload, payload.length);
    }

    public short getApiKey() {
        return apiKey;
    }

    public short getApiVersion() {
        return apiVersion;
    }

    public int getCorrelationId() {
        return correlationId;
    }

    public String getClientId() {
        return clientId;
    }

    public int getPayloadLength() {
        return payload.length;
    }

    public byte[] getPayload() {
        return Arrays.copyOf(payload, payload.length);
    }
}