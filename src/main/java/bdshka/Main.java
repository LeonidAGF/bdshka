package bdshka;

import bdshka.dispatcher.Dispatcher;
import bdshka.server.Server;

import static bdshka.Constants.PORT;

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
