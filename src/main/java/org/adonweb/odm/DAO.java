package org.adonweb.odm;

/**
 * @author Artur Khakimov <djion@ya.ru>
 */
public interface DAO<T> {

    /**
     * Return objects by key
     * @param key key for select
     * @return result QueryResult with object
     * @throws Exception depends on Connection
     */
    QueryResult<T> select(Object key) throws Exception;

    QueryResult<T> select(Integer index, Object key) throws Exception;

    /**
     * Insert entity to storage
     * @param entity for insert
     * @return result, depends on Connection
     * @throws Exception depends on Connection
     */
    QueryResult<T> insert(T entity) throws Exception;

    /**
     * Save/Update entity (by internal key)
     * @param entity for save/update
     * @return result, depends on Connection
     * @throws Exception depends on Connection
     */
    QueryResult<T> save(T entity) throws Exception;

    /**
     * Remove entity (by internal key)
     * @param entity for remove
     * @return result, depends on Connection
     * @throws Exception depends on Connection
     */
    QueryResult<T> delete(T entity) throws Exception;

    /**
     * Return full set of entities
     * @return QueryResult with data
     * @throws Exception depends on Connection
     */
    QueryResult<T> all() throws Exception;
}
