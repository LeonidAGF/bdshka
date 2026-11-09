package org.bdshka.server.protocol.session;

import org.bdshka.engine.QueryResult;
import java.util.List;

public interface Portal {
    String name();
    PreparedStatement statement();
    List<Object> params();                  //уже декодированные параметры
    List<QueryResult> result();              //ленивый вариант добавим позже
}