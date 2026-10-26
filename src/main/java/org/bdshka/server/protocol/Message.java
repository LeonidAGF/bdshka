package org.bdshka.server.protocol;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public final class Message {
    private final byte type;
    private final byte[] payload;
}