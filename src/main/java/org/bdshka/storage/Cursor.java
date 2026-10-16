package org.bdshka.storage;

/*
 *
 * Читаем что-то из бд по страницам а не сразу загружая всё в память + если и отдаём что-то тому кто хоче прочтить что-то со страницы то
 * тоже лишь небольшую часть (1 Record) а не всё с страницы стразу
 *
 */
public interface Cursor {
    boolean hasNext();
    Record next();
    void close();
}
