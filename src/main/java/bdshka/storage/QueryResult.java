package bdshka.storage;

import java.util.List;

public interface QueryResult {
    Schema getSchema();
    List<Data> getRows();
    String getCommandTag();
    boolean isSuccess();
    String getErrorMessage();
}
