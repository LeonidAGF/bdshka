package org.bdshka.server.protocol;

public final class Protocol {
    private Protocol() {}

    // Версия протокола
    public static final int PROTOCOL_VERSION_3_0 = 196608;

    // Frontend → Backend
    public static final byte MSG_QUERY       = 'Q';
    public static final byte MSG_PARSE       = 'P';
    public static final byte MSG_BIND        = 'B';
    public static final byte MSG_EXECUTE     = 'E';
    public static final byte MSG_DESCRIBE    = 'D';
    public static final byte MSG_CLOSE       = 'C';
    public static final byte MSG_SYNC        = 'S';
    public static final byte MSG_FLUSH       = 'H';
    public static final byte MSG_TERMINATE   = 'X';
    public static final byte MSG_PASSWORD    = 'p';

    // Backend → Frontend
    public static final byte MSG_AUTH              = 'R';
    public static final byte MSG_PARAMETER_STATUS  = 'S';
    public static final byte MSG_BACKEND_KEY_DATA  = 'K';
    public static final byte MSG_READY_FOR_QUERY   = 'Z';
    public static final byte MSG_ROW_DESCRIPTION   = 'T';
    public static final byte MSG_DATA_ROW          = 'D';
    public static final byte MSG_COMMAND_COMPLETE  = 'C';
    public static final byte MSG_EMPTY_QUERY       = 'I';
    public static final byte MSG_ERROR             = 'E';
    public static final byte MSG_NOTICE            = 'N';
    public static final byte MSG_PARSE_COMPLETE    = '1';
    public static final byte MSG_BIND_COMPLETE     = '2';
    public static final byte MSG_CLOSE_COMPLETE    = '3';
    public static final byte MSG_NO_DATA           = 'n';
    public static final byte MSG_PARAM_DESCRIPTION = 't';
    public static final byte MSG_PORTAL_SUSPENDED  = 's';

    // Коды AuthenticationRequest
    public static final int AUTH_OK                 = 0;
    public static final int AUTH_CLEARTEXT_PASSWORD = 3;
    public static final int AUTH_MD5_PASSWORD       = 5;

    // Статус транзакции в ReadyForQuery
    public static final byte TX_IDLE            = 'I';
    public static final byte TX_IN_TRANSACTION  = 'T';
    public static final byte TX_FAILED          = 'E';

    // OID'ы типов PostgreSQL (для RowDescription)
    public static final int OID_BOOL    = 16;
    public static final int OID_INT4    = 23;
    public static final int OID_INT8    = 20;
    public static final int OID_FLOAT8  = 701;
    public static final int OID_VARCHAR = 1043;
    public static final int OID_TEXT    = 25;
}