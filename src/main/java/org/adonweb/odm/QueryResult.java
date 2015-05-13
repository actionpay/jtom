package org.adonweb.odm;

import java.util.Iterator;
import java.util.List;

/**
 * @author Artur Khakimov <djion@ya.ru>
 */
public interface QueryResult<T>{
    List getAsPlainList();
    List<T> getAsObjectList() throws Exception;
    Iterator getPlainIterator();
    Iterator<T> getObjectIterator() throws Exception;
}
