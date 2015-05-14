package net.actionpay.jtom.tarantool;

import net.actionpay.jtom.*;
import net.actionpay.jtom.Field;

import java.util.*;
import java.util.List;

/**
 * Tarantool DAO wrapper
 *
 * @author Artur Khakimov <djion@ya.ru>
 */
public class TarantoolDAOImpl<T> implements DAO<T> {

    private Class<? extends T> entityClass;
    private Map<Integer, java.lang.reflect.Field> fields = new HashMap<>();
    private Integer fieldsCount = -1;
    private Map<Integer, Map<Integer, java.lang.reflect.Field>> keys = new HashMap<>();
    private Map<Integer, Index> indexMap = new HashMap<>();
    private Connection link;
    private String space;
    private Integer spaceId;
    protected static HashMap<Class<?>, DAO> pool = new HashMap<>();


    public static DAO getByClass(Class<?> entityClass) throws Exception {
        pool.putIfAbsent(entityClass, new TarantoolDAOImpl<>(entityClass));
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
            fieldsCount = Integer.max(fieldPosition + 1, fieldsCount);
            if (!field.isAccessible())
                field.setAccessible(true);
            if (fields.containsKey(tarantoolField.position()))
                throw new Exception("Cannot be 2 or more fields with same position: " + field.getName());
            fields.put(fieldPosition, field);
            validateFieldClass(field.getType());
        }
    }

    private void keyStore(Key key, java.lang.reflect.Field field) throws Exception {
        if (key != null) {
            Integer index = key.index();
            Integer position = key.position();
            keys.putIfAbsent(index, new HashMap<>());
            if (keys.get(index).containsKey(position))
                throw new Exception("Cannot be 2 or more keys with same position " + position + " and same index "
                        + index + ": " + field.getName());
            keys.get(index).put(position, field);
        }
    }

    /**
     * Should be run after link connection created.
     */
    private void initSpaceId() throws Exception {

        List result = ((TarantoolConnection) link).eval("box_space_" + space + " = box.space." + space + "\n"
                + "if not box_space_" + space + " then return null end\n"
                + "return box.space." + space + ".id");
        spaceId = (Integer) result.get(0);

    }

    protected TarantoolDAOImpl(Class<? extends T> entityClass) throws Exception {
        this.entityClass = entityClass;
        Entity tEntityAnnotation = entityClass.getDeclaredAnnotation(Entity.class);
        if (null == tEntityAnnotation)
            throw new Exception("Entity annotation is not set for class " + entityClass.getName());
        Indexes indexes = entityClass.getDeclaredAnnotation(Indexes.class);
        if (null == indexes)
            throw new Exception("Indexes annotation is not set for class " + entityClass.getName());
        indexStore(indexes);
        link = ConnectionPool.connection(tEntityAnnotation.connection());

        for (java.lang.reflect.Field field : entityClass.getDeclaredFields()) {
            Field tarantoolField = field.getDeclaredAnnotation(Field.class);
            Key key = field.getDeclaredAnnotation(Key.class);
            if (key != null && tarantoolField == null)
                throw new Exception("Key field " + field.getName()
                        + " should have annotation @" + Field.class.getName());
            fieldStore(tarantoolField, field);
            keyStore(key, field);

        }
        space = tEntityAnnotation.space();
        initSpaceId();
    }

    private void indexStore(Indexes indexes) {
        int i = 0;
        for (Index index : indexes.value())
            indexMap.put(i++, index);
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
        List result = ((TarantoolConnection) link).select(spaceId, index, value, offset, limit, 0);
        return new TarantoolQueryResult<>(entityClass, result);
    }

    public QueryResult<T> all() throws Exception {
        return new TarantoolQueryResult<>(entityClass, ((TarantoolConnection) link)
                .select(spaceId, 0, Collections.singletonList(0), 0, Integer.MAX_VALUE, 2));
    }

    @Override
    public QueryResult<T> createSpace() throws Exception {
        StringBuilder query = new StringBuilder();
        query.append("box.schema.space.create('").append(space).append("')\n");

        for (Integer indexId : indexMap.keySet()) {
            Index index = indexMap.get(indexId);
            query.append("box.space.")
                    .append(space)
                    .append(":create_index('")
                    .append(null == index.name() ? indexId : index.name())
                    .append("', {type = '").append(indexTypeToString(index.indexType())).append("', ")
                    .append("unique = ").append(String.valueOf(index.unique())).append(" , parts = {");
            for (Integer id : keys.get(indexId).keySet()) {
                java.lang.reflect.Field field = keys.get(indexId).get(id);
                query.append(id).append(", '").append(typeToKeyType(field.getType())).append("'");
            }
            query.append("}})\n");
        }
        QueryResult<T> result = new TarantoolQueryResult<>(entityClass, ((TarantoolConnection) link)
                .eval(query.toString()));
        initSpaceId();
        return result;
    }

    @Override
    public QueryResult<T> dropSpace() throws Exception {
        return new TarantoolQueryResult<>(entityClass, ((TarantoolConnection) link)
                .eval("box.space." + space + ":drop()"));
    }

    @Override
    public QueryResult<T> dropById(Object id) throws Exception{
        return new TarantoolQueryResult<>(entityClass, ((TarantoolConnection) link)
                .delete(spaceId, Arrays.asList(0, id)));
    }

    @Override
    public QueryResult<T> getById(Object id) throws Exception {
        return get(id);
    }

    public QueryResult<T> get(Object key) throws Exception {
        return get(0, key);
    }


    public QueryResult<T> get(Integer index, Object key) throws Exception {
        QueryResult<T> objects;
        if (index == null)
            objects = find(0, Collections.singletonList(key));
        else if (!(key instanceof List)) {
            objects = find(index, Collections.singletonList(key));
        } else
            objects = find(index, key);

        return objects;
    }

    private List entityToList(T entity) throws IllegalAccessException {
        List<Object> data = new ArrayList<>();
        for (int i = 0; i < fieldsCount; i++)
            if (fields.containsKey(i))
                data.add(fields.get(i).get(entity));
            else
                data.add(null);
        return data;
    }

    private List indexToList(Integer index, T entity) throws IllegalAccessException {
        List<Object> data = new ArrayList<>();
        Map<Integer, java.lang.reflect.Field> keysMap = keys.get(index);
        for (Integer key : keysMap.keySet())
            data.add(keysMap.get(key).get(entity));
        return data;
    }


    String indexTypeToString(IndexType type) throws Exception {
        switch (type) {
            case INDEX_TYPE_HASH:
                return "HASH";
            case INDEX_TYPE_BITSET:
                return "BITSET";
            case INDEX_TYPE_RTREE:
                return "RTREE";
            case INDEX_TYPE_TREE:
                return "TREE";
            default:
                throw new Exception();
        }
    }


    String typeToKeyType(Class<?> type) throws Exception {
        if (Number.class.isAssignableFrom(type)) {
            return "NUM";
        }
        if (String.class.isAssignableFrom(type)) {
            return "STR";
        }
        if (List.class.isAssignableFrom(type)) {
            return "ARRAY";
        }
        throw new Exception();

    }

    private static Number narrovingNumberConversion(Class<?> outputType, Number value) throws Exception {
        if (value == null) {
            return null;
        }
        if (Byte.class.equals(outputType)) {
            return value.byteValue();
        }
        if (Short.class.equals(outputType)) {
            return value.shortValue();
        }
        if (Integer.class.equals(outputType)) {
            return value.intValue();
        }
        if (Long.class.equals(outputType)) {
            return value.longValue();
        }
        if (Float.class.equals(outputType)) {
            return value.floatValue();
        }
        if (Double.class.equals(outputType)) {
            return value.doubleValue();
        }
        throw new Exception();

    }

    public List<T> convertPlainListToObjectList(List objects) throws Exception {
        List<T> result = new ArrayList<>();
        for (Object obj : objects) {
            T instance = entityClass.getConstructor().newInstance();
            for (java.lang.reflect.Field field : fields.values()) {
                Object fieldValue = (((List) (obj)).get(field.getDeclaredAnnotation(Field.class).position()));
                if (fieldValue instanceof Number && Number.class.isAssignableFrom(field.getType()))
                    field.set(instance, narrovingNumberConversion(field.getType(), (Number) fieldValue));
                else
                    field.set(instance, fieldValue);
            }
            result.add(instance);
        }
        return result;
    }

    public QueryResult<T> add(T entity) throws Exception {
        return new TarantoolQueryResult<>(entityClass, ((TarantoolConnection) link)
                .insert(spaceId, entityToList(entity)));
    }

    public QueryResult<T> save(T entity) throws Exception {
        return new TarantoolQueryResult<>(entityClass, ((TarantoolConnection) link)
                .replace(spaceId, entityToList(entity)));
    }

    public QueryResult<T> drop(T entity) throws IllegalAccessException {
        return new TarantoolQueryResult<>(entityClass, ((TarantoolConnection) link)
                .delete(spaceId, indexToList(0, entity)));
    }


}


