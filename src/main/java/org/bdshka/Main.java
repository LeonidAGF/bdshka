package org.bdshka;

import org.bdshka.server.Server;
import org.bdshka.server.auth.AuthMethod;

import static org.bdshka.Constants.PORT;

public class Main {
    /**
     * app start point
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        Server server = new Server(PORT, null, AuthMethod.parse(String.valueOf(AuthMethod.MD5)).create());
        server.start();
    }
}
