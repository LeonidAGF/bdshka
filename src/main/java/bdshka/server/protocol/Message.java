package bdshka.server.protocol;

import java.util.Arrays;

/**
 * Represents a decoded network packet for a Kafka-compatible request.
 *
 * This class is immutable. The payload is stored defensively as a copy
 * to prevent external modification after construction.
 */
public final class Message {

    private final short apiKey;
    private final short apiVersion;
    private final int correlationId;
    private final String clientId;
    private final byte[] payload;

    /**
     * Creates a new packet.
     *
     * @param apiKey        Kafka API key identifying the request type
     * @param apiVersion    Kafka API version of the request
     * @param correlationId correlation identifier that must be returned in the response
     * @param clientId      client identifier; may be {@code null}
     * @param payload       request payload body; if {@code null}, an empty payload is used
     */
    public Message(short apiKey, short apiVersion, int correlationId, String clientId, byte[] payload) {
        this.apiKey = apiKey;
        this.apiVersion = apiVersion;
        this.correlationId = correlationId;
        this.clientId = clientId;
        this.payload = payload == null ? new byte[0] : Arrays.copyOf(payload, payload.length);
    }

    /**
     * Returns the Kafka API key.
     *
     * @return API key
     */
    public short getApiKey() {
        return apiKey;
    }

    /**
     * Returns the Kafka API version.
     *
     * @return API version
     */
    public short getApiVersion() {
        return apiVersion;
    }

    /**
     * Returns the correlation identifier.
     *
     * <p>The same correlation identifier must be included in the corresponding response.
     *
     * @return correlation identifier
     */
    public int getCorrelationId() {
        return correlationId;
    }

    /**
     * Returns the client identifier.
     *
     * @return client identifier, or {@code null} if not provided
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Returns the payload length in bytes.
     *
     * @return payload length
     */
    public int getPayloadLength() {
        return payload.length;
    }

    /**
     * Returns a copy of the request payload.
     *
     * <p>Modifying the returned array does not affect the internal packet state.
     *
     * @return copy of the payload
     */
    public byte[] getPayload() {
        return Arrays.copyOf(payload, payload.length);
    }
}