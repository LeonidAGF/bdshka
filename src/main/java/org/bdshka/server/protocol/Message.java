package org.bdshka.server.protocol;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public final class Message {

    private byte massageId;
    private int length;
    private byte[] payload;

}