package bdshka.storage;

public interface Data {
    Object getValue(int columnIndex);
    String getString(int columnIndex);
    int getInt(int columnIndex);
    boolean isNull(int columnIndex);
    Schema getSchema();
    byte[] serialize();
}