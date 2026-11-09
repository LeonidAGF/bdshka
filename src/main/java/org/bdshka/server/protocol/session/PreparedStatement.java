package org.bdshka.server.protocol.session;

import java.util.List;

public interface PreparedStatement {
    String name();
    String sql();
    List<Integer> paramOids();   //из ParameterDescription
}