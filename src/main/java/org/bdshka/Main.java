package org.bdshka;

import org.bdshka.dispatcher.Dispatcher;
import org.bdshka.server.Server;

import static org.bdshka.Constants.PORT;

public class Main {
    /**
     * app start point
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        Dispatcher dispatcher = null;
        Server bk = new Server(PORT,dispatcher);
        bk.start();
    }
}
