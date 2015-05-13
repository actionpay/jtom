package org.adonweb.odm;

/**
 * @author Artur Khakimov <djion@ya.ru>
 */
public interface DAO<T> {

    QueryResult<T> select(Object key) throws Exception;
    QueryResult<T> select(Integer index, Object key) throws Exception;
    QueryResult<T> insert(T entity) throws Exception;
    QueryResult<T> save(T entity) throws Exception;
    QueryResult<T> delete(T entity) throws Exception;
    QueryResult<T> all() throws Exception;
}
