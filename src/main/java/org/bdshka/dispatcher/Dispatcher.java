package org.bdshka.dispatcher;


import org.bdshka.server.protocol.Message;

public interface Dispatcher {
    Message dispatch(Message request);
}
