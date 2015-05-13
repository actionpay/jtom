package org.adonweb.odm.tarantool;

import org.adonweb.odm.QueryResult;

import java.util.Iterator;
import java.util.List;

/**
 * Implementation of tarantool query result
 * @author Artur Khakimov <djion@ya.ru>
 */
public class TarantoolQueryResult<T> implements QueryResult<T> {
    Class<? extends T> entityClass;
    List result;
    public TarantoolQueryResult(Class<? extends T>entityClass, List result) {
        this.entityClass = entityClass;
        this.result = result;
    }

    @Override
    public List getAsPlainList() {
        return result;
    }

    @Override
    public List<T> getAsObjectList() throws Exception {
        return ((TarantoolDAOImpl<T>)TarantoolDAOImpl.getByClass(entityClass)).convertPlainListToObjectList(result);
    }

    @Override
    public Iterator getPlainIterator() {
        return result.iterator();
    }

    @Override
    public Iterator<T> getObjectIterator() throws Exception {
        return ((TarantoolDAOImpl<T>)TarantoolDAOImpl.getByClass(entityClass)).convertPlainListToObjectList(result).iterator();
    }
}
