package org.bdshka.server.handlers;

import java.io.IOException;

import org.bdshka.engine.QueryResult;
import org.bdshka.server.protocol.MessageWriter;
import org.bdshka.server.protocol.server.CommandComplete;
import org.bdshka.server.protocol.server.DataRow;
import org.bdshka.server.protocol.server.EmptyQueryResponse;
import org.bdshka.server.protocol.server.ErrorResponse;
import org.bdshka.server.protocol.server.RowDescription;

public final class QueryResultEncoder {

    private static final String SEPARATOR = " ";

    public void encode(MessageWriter writer, QueryResult result) throws IOException {
        if (result instanceof QueryResult.Rows rows) {
            writer.send(new RowDescription(rows.columns()));
            for (Object[] row : rows.rows()) {
                writer.send(new DataRow(row));
            }
            writer.send(new CommandComplete(commandTag(rows)));
        } else if (result instanceof QueryResult.Command command) {
            writer.send(new CommandComplete(command.tag()));
        } else if (result instanceof QueryResult.Empty) {
            writer.send(new EmptyQueryResponse());
        } else if (result instanceof QueryResult.ErrorResult error) {
            writer.send(ErrorResponse.of(error.sqlState(), error.message()));
        }
    }

    private static String commandTag(QueryResult.Rows rows) {
        if (rows.tag() != null && !rows.tag().isBlank()) {
            return rows.tag();
        }
        return String.join(SEPARATOR, "SELECT", String.valueOf(rows.rows().size()));
    }
}