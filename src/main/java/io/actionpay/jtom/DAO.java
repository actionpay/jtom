package io.actionpay.jtom;

import java.util.List;

/**
 * @author Artur Khakimov <djion@ya.ru>
 */
public interface DAO<T> {

	/**
	 * Return objects by key
	 *
	 * @param key key for get
	 * @return result QueryResult with object
	 * @throws Exception depends on Connection
	 */
	QueryResult<T> get(Object key) throws Exception;

	/**
	 * @param index index
	 * @param key   fields values as index key
	 * @return QueryResult with objects equals to index key
	 * @throws Exception
	 */
	QueryResult<T> get(Integer index, Object key) throws Exception;

	/**
	 * Insert entity to storage
	 *
	 * @param entity for add
	 * @return result, depends on Connection
	 * @throws Exception depends on Connection
	 */
	QueryResult<T> add(T entity) throws Exception;

	/**
	 * Save/Update entity (by internal key)
	 *
	 * @param entity for save/update
	 * @return result, depends on Connection
	 * @throws Exception depends on Connection
	 */
	QueryResult<T> save(T entity) throws Exception;

	/**
	 * Remove entity (by internal key)
	 *
	 * @param entity for remove
	 * @return result, depends on Connection
	 * @throws Exception depends on Connection
	 */
	QueryResult<T> drop(T entity) throws Exception;

	/**
	 * Return full set of entities
	 *
	 * @return QueryResult with data
	 * @throws Exception depends on Connection
	 */
	QueryResult<T> all() throws Exception;

	/**
	 * Create space scheme for Entity
	 *
	 * @return depends on Connection
	 * @throws Exception
	 */
	QueryResult<T> createSpace() throws Exception;

	/**
	 * Drop space scheme for Entity
	 *
	 * @return depends on Connection
	 * @throws Exception
	 */
	QueryResult<T> dropSpace() throws Exception;

	/**
	 * Drop object by id
	 *
	 * @return depends on Connection
	 * @throws Exception
	 */
	QueryResult<T> dropById(Object id) throws Exception;

	/**
	 * Get object by id
	 *
	 * @return depends on Connection
	 * @throws Exception
	 */
	QueryResult<T> getById(Object id) throws Exception;

	/**
	 *
	 * @param key Object Key
	 * @return Object<T> by Key
	 * @throws Exception implementation depended
	 */
	T one(Object key) throws Exception;

	/**
	 *
	 * @param index index name
	 * @param object entity
	 * @return many for object key
	 * @throws Exception
	 */
	List<T> many(String index, Object object) throws Exception;

	/**
	 * Return list of keys for index
	 *
	 * @param index index Id
	 * @param entity entity
	 * @return List of keys
	 * @throws Exception
	 */
	List indexToList(Integer index, T entity) throws Exception;

	<T2> void setProperty(T entity, String propertyName, T2 value, Class<T2> propertyClass) throws Exception;

	<T2> T2 getProperty(T entity, String propertyName, Class<T2> propertyClass) throws Exception;
}
