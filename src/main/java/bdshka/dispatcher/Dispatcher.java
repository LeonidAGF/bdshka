package bdshka.dispatcher;


import bdshka.server.protocol.Message;

public interface Dispatcher {
    Message dispatch(Message request);
}
