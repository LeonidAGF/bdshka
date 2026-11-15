Startup message:
int32 length 4byte
int32 protocol version 4byte
params:
user-имя пользователя postgres
database-база, к которой подключаемся testdb
client_encoding-кодировка клиента UTF8
application_name-имя приложения  psql, pgjdbc
options-runtime-параметры search_path=myschema
replication-режим репликации

base message:
byte type
int32 length 4byte
payload 4byte