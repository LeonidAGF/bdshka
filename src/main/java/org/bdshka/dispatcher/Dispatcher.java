package org.bdshka.dispatcher;


import org.bdshka.server.protocol.Message;

public interface Dispatcher {
    //execute(sql) → Result
    Message dispatch(Message request);
}
