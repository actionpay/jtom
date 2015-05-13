package org.adonweb.odm.tarantool;

import org.adonweb.odm.*;

import java.util.*;
import java.util.List;

/**
 * Tarantool DAO wrapper
 * @author Artur Khakimov <djion@ya.ru>
 */
public class TarantoolDAOImpl<T> implements DAO<T> {
    private Class<? extends T> entityClass;
    private Map<Integer, java.lang.reflect.Field> fields = new HashMap<>();
    private Integer fieldsCount =-1;
    private Map<Integer, Map<Integer, java.lang.reflect.Field>> keys = new HashMap<>();
    private Connection link;
    private Integer spaceId;
    protected static HashMap<Class<?>, DAO> pool = new HashMap<>();


    public static DAO getByClass(Class<?> entityClass) throws Exception {
        if (!pool.containsKey(entityClass)) {
            pool.put(entityClass, new TarantoolDAOImpl<>(entityClass));
        }
        return pool.get(entityClass);
    }

    void validateFieldClass(Class<?> fClass) throws Exception {
        if (!((Number.class.isAssignableFrom(fClass))
                || String.class.isAssignableFrom(fClass)
                || Map.class.isAssignableFrom(fClass)
                || List.class.isAssignableFrom(fClass)))
            throw new Exception("Class " + fClass + " not supported by Tarantool");
    }

    private void fieldStore(Field tarantoolField, java.lang.reflect.Field field) throws Exception {
        if (tarantoolField != null) {
            Integer fieldPosition = tarantoolField.position();
            fieldsCount = Integer.max(fieldPosition+1, fieldsCount);
            if (!field.isAccessible())
                field.setAccessible(true);
            if (fields.containsKey(tarantoolField.position()))
                throw new Exception("Cannot be 2 or more fields with same position: "+field.getName());
            fields.put(fieldPosition,field);
            validateFieldClass(field.getType());
        }
    }

    private void indexStore(Key key, java.lang.reflect.Field field) throws Exception {
        if (key != null)
        {
            Integer index = key.index();
            Integer position = key.position();

            if (!keys.containsKey(index))
                keys.put(index, new HashMap<>());
            if (keys.get(index).containsKey(position))
                throw new Exception("Cannot be 2 or more keys with same position "+position+" and same index "+index+": "+field.getName());
            keys.get(index).put(position, field);
        }
    }

    /**
     * Should be run after link connection created.
     */
    private void initSpaceId(Entity tarantoolEntity){
        String space = tarantoolEntity.space();
        spaceId = (Integer) ((TarantoolConnection)link).eval("return box.space." + space + ".id").get(0);

    }

    protected TarantoolDAOImpl(Class<? extends T> entityClass) throws Exception {
        this.entityClass = entityClass;
        Entity tEntityAnnotation = entityClass.getDeclaredAnnotation(Entity.class);
        if (null == tEntityAnnotation)
            throw new Exception("Entity annotation is not set for class " + entityClass.getName());
        link = ConnectionPool.connection(tEntityAnnotation.connection());
        initSpaceId(tEntityAnnotation);
        for (java.lang.reflect.Field field : entityClass.getDeclaredFields()) {
            Field tarantoolField = field.getDeclaredAnnotation(Field.class);
            Key key = field.getDeclaredAnnotation(Key.class);
            if (key != null && tarantoolField == null)
                throw new Exception("Key field " + field.getName() + " should have annotation @" + Field.class.getName());
            fieldStore(tarantoolField,field);
            indexStore(key,field);
        }
    }

    public QueryResult<T> find(int index, Object value) {
        return find(index, value, Integer.MAX_VALUE);
    }

    public QueryResult<T> find(int index, Object value, int limit) {
        return find(index, value, limit, 0);
    }

    public QueryResult<T> find(int index, Object value, Integer limit, Integer offset) {
        List<Object> args = Arrays.asList(spaceId, value, index, 0, 0);
        if (limit >= 0) {
            args.set(3, limit);
            if (offset >= 0) {
                args.set(4, offset);
            }
        }
        List result = ((TarantoolConnection)link).select(spaceId, index, value, offset, limit, 0);
        return new TarantoolQueryResult<>(entityClass, result);
    }

    public QueryResult<T> all() throws Exception {
        return new TarantoolQueryResult<>(entityClass,((TarantoolConnection)link).select(spaceId, 0, Collections.singletonList(0), 0, Integer.MAX_VALUE, 2));
    }

    public QueryResult<T> select(Object key) throws Exception {
        return select(0, key);
    }



    public QueryResult<T> select(Integer index, Object key) throws Exception {
        QueryResult<T> objects;
        if (index == null)
            objects = find(0, Collections.singletonList(key));
        else
        if (!(key instanceof List)) {
            objects = find(index, Collections.singletonList(key));
        } else
            objects = find(index, key);

        return objects;
    }

    private List entityToList(T entity) throws IllegalAccessException {
        List<Object> data = new ArrayList<>();
        for (int i=0;i< fieldsCount;i++)
            if (fields.containsKey(i))
                data.add(fields.get(i).get(entity));
            else
                data.add(null);
        return data;
    }

    private List indexToList(Integer index, T entity) throws IllegalAccessException {
        List<Object> data = new ArrayList<>();
        Map<Integer, java.lang.reflect.Field> keysMap =  keys.get(index);
        for (Integer key:keysMap.keySet())
            data.add(keysMap.get(key).get(entity));
        return data;
    }

    private static Number narrovingNumberConversion(Class<?> outputType, Number value) throws Exception {
        if(value == null) {
            return null;
        }
        if(Byte.class.equals(outputType)) {
            return value.byteValue();
        }
        if(Short.class.equals(outputType)) {
            return value.shortValue();
        }
        if(Integer.class.equals(outputType)) {
            return value.intValue();
        }
        if(Long.class.equals(outputType)) {
            return value.longValue();
        }
        if(Float.class.equals(outputType)) {
            return value.floatValue();
        }
        if(Double.class.equals(outputType)) {
            return value.doubleValue();
        }
        throw new Exception();

    }

    public List<T> convertPlainListToObjectList(List objects) throws Exception {
        List<T> result = new ArrayList<>();
        for (Object obj : objects) {
            T instance = entityClass.getConstructor().newInstance();
            for (java.lang.reflect.Field field:fields.values())
            {
                Object fieldValue = (((List) (obj)).get(field.getDeclaredAnnotation(Field.class).position()));
                if (fieldValue instanceof Number && Number.class.isAssignableFrom(field.getType()))
                    field.set(instance,narrovingNumberConversion(field.getType(),(Number)fieldValue));
                else
                    field.set(instance,fieldValue);
            }
            result.add(instance);
        }
        return result;
    }

    public QueryResult<T> insert(T entity) throws Exception {
        return new TarantoolQueryResult<>(entityClass,((TarantoolConnection)link).insert(spaceId, entityToList(entity)));
    }

    public QueryResult<T> save(T entity) throws Exception {
        return new TarantoolQueryResult<>(entityClass,((TarantoolConnection)link).replace(spaceId, entityToList(entity)));
    }

    public QueryResult<T> delete(T entity) throws IllegalAccessException {
        return new TarantoolQueryResult<>(entityClass,((TarantoolConnection)link).delete(spaceId, indexToList(0, entity)));
    }


}


