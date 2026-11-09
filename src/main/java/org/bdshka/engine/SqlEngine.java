package org.bdshka.engine;

public interface SqlEngine {
    //Новая сессия на каждое соединение
    EngineSession openSession();
}
