package io.actionpay.jtom;

import java.util.Iterator;
import java.util.List;

/**
 * Query result
 *
 * @author Artur Khakimov <djion@ya.ru>
 */
public interface QueryResult<T> {

	/**
	 * Return plain data list of query result
	 *
	 * @return list of plain data
	 */
	List getAsPlainList();

	/**
	 * Return data converted to objects list of query result
	 *
	 * @return list of object data
	 */
	List<T> getAsObjectList() throws Exception;

	/**
	 * Return list iterator plain data
	 *
	 * @return iterator
	 */
	Iterator getPlainIterator();

	/**
	 * Return list iterator typed object data
	 *
	 * @return iterator
	 */
	Iterator<T> getObjectIterator() throws Exception;
}
