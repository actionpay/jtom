package io.actionpay.jtom.tarantool;

import com.sun.org.apache.xpath.internal.functions.WrongNumberArgsException;
import io.actionpay.jtom.*;
import io.actionpay.jtom.annotations.*;
import io.actionpay.jtom.annotations.Properties;
import io.actionpay.jtom.tarantool.exception.*;
import io.actionpay.jtom.exception.InvalidFieldClassException;
import io.actionpay.jtom.exception.NonStaticMethodException;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.List;

/**
 * Tarantool DAO wrapper
 *
 * @author Artur Khakimov <djion@ya.ru>
 */
public class TarantoolImpl<T> extends CallHandlerImpl implements DAO<T>, CallHandler {
	//space Id for properties
	Map<String, Integer> propertyMapSpace = new HashMap<>();
	//connections for properties
	Map<String, Connection> propertyMapConnection = new HashMap<>();
	// dao entity class
	private Class<? extends T> entityClass;
	// field position - field link
	private Map<Integer, java.lang.reflect.Field> fields = new HashMap<>();
	// field - field position link
	private Map<java.lang.reflect.Field, Integer> fieldPositions = new HashMap<>();
	// field - serializer link
	SerializeManager serializeManager = SerializeManager.getInstance();
	// max count fields (calculated value)
	private Integer fieldsCount = -1;
	//index - key map link
	private Map<Integer, Map<Integer, java.lang.reflect.Field>> keys = new HashMap<>();
	//
	private Map<Integer, Index> indexMap = new HashMap<>();
	//index name - index position link
	private Map<String, Integer> indexPositions = new HashMap<>();
	//dao connection
	private Connection link;
	//space name
	private String space;
	//space id (caculated value)
	private Integer spaceId;
	//null key object (calculated value)
	private Object emptyKey;
	//dao pool
	protected static HashMap<Class<?>, DAO> pool = new HashMap<>();
	//possible handlers
	private List<Class<? extends Annotation>> handlers = Arrays.asList(AfterAdd.class, AfterDrop.class, AfterGet.class, AfterSave.class,
			BeforeAdd.class, BeforeGet.class, BeforeDrop.class, BeforeSave.class);

	List<Class<?>> serializableClasses = Arrays.asList(Number.class, String.class, Map.class, List.class
			, Boolean.class, double.class, long.class, float.class, boolean.class, int.class);

	public static DAO getByClass(Class<?> entityClass) throws Exception {
		pool.putIfAbsent(entityClass, new TarantoolImpl<>(entityClass));
		return pool.get(entityClass);
	}

	void validateFieldClass(Class<?> fClass) throws InvalidFieldClassException {
		for (Class<?> clazz : serializableClasses)
			if (clazz.isAssignableFrom(fClass))
				return;
		throw new InvalidFieldClassException("Class " + fClass + " not supported by Tarantool");
	}


	private void initProperties() throws Exception {
		Properties properties = entityClass.getAnnotation(Properties.class);
		if (properties == null)
			return;
		for (Property property : properties.value()) {
			String propertySpace = property.space();
			propertyMapConnection.putIfAbsent(propertySpace, ConnectionPool.connection(property.connection()));
			Integer spaceId = getSpaceId(propertyMapConnection.get(propertySpace), propertySpace);
			if (spaceId == null)
				createPropertySpace(property);
			spaceId = getSpaceId(propertyMapConnection.get(propertySpace), propertySpace);
			if (spaceId == null)
				throw new Exception("Cant find/create space: " + propertySpace);
			propertyMapSpace.putIfAbsent(propertySpace, spaceId);
		}

	}

	private void createPropertySpace(Property property) throws Exception {
		StringBuilder query = new StringBuilder();
		query.append("box.schema.space.create('").append(property.space()).append("')\n")
				.append(buildSpaceIndexCreateEvalExpression(0, property.space(), true));
		((TarantoolConnection) propertyMapConnection.get(property.space())).eval(query.toString());
	}

	private void fieldStore(io.actionpay.jtom.annotations.Field tarantoolField, java.lang.reflect.Field field) throws Exception {
		if (tarantoolField != null) {
			Integer fieldPosition = tarantoolField.position();
			fieldsCount = Integer.max(fieldPosition + 1, fieldsCount);
			if (!field.isAccessible())
				field.setAccessible(true);
			if (fields.containsKey(tarantoolField.position()))
				throw new WrongTarantoolFieldPositionException("Cannot be 2 or more fields with same position: " + field.getName());
			fields.put(fieldPosition, field);
			fieldPositions.put(field, fieldPosition);
			validateFieldClass(field.getType());
		}
	}

	/**
	 * Save key annotated field for internal uses
	 *
	 * @param key   key annotation
	 * @param field field with key annotation
	 * @throws Exception if 2 keys have same position and same index
	 */
	private void keyStore(Key key, java.lang.reflect.Field field) throws Exception {
		if (key != null) {
			if (!indexPositions.containsKey(key.index()))
				throw new UndeclaredKeyIndexException("Key index " + key.index() + " not declared in @Indexes");
			Integer index = indexPositions.get(key.index());
			Integer position = key.position();
			keys.putIfAbsent(index, new HashMap<>());
			if (keys.get(index).containsKey(position))
				throw new WrongKeyIndexException("Cannot be 2 or more keys with same position " + position + " and same index "
						+ index + ": " + field.getName());
			keys.get(index).put(position, field);
		}
	}

	private Integer getSpaceId(Connection link, String space) throws Exception {
		List result = ((TarantoolConnection) link).eval("box_space_" + space + " = box.space." + space + "\n"
				+ "if not box_space_" + space + " then return null end\n"
				+ "return box.space." + space + ".id");
		spaceId = (Integer) result.get(0);
		return spaceId;
	}

	/**
	 * Init space id code if space exists
	 *
	 * @throws Exception if some Tarantool magic don't work
	 */
	private void initSpaceId() throws Exception {
		spaceId = getSpaceId(link, space);

	}

	private void prepareEmptyKeys() {
		emptyKey = new ArrayList<>();
		keys.get(0).keySet().stream().sorted().forEach(key -> {
			try {
				Class<?> field = keys.get(0).get(key).getType();
				//todo: dirty move
				if (Number.class.isAssignableFrom(field))
					((List) emptyKey).add(field.getConstructors()[0].newInstance(0));
				else
					((List) emptyKey).add(field.newInstance());
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	/**
	 * Process Entity, Indexes, Key, Field annotations, try to get space Id,
	 * establish connection if not exist and set link to connection
	 *
	 * @throws Exception
	 */
	private void init() throws Exception {
		Entity tEntityAnnotation = entityClass.getDeclaredAnnotation(Entity.class);
		if (null == tEntityAnnotation)
			throw new EntityAnnotationMissException("Entity annotation is not set for class " + entityClass.getName());
		Indexes indexes = entityClass.getDeclaredAnnotation(Indexes.class);
		if (null == indexes)
			throw new IndexAnnotationMissException("Indexes annotation is not set for class " + entityClass.getName());
		indexStore(indexes);
		setLink(ConnectionPool.connection(tEntityAnnotation.connection()));
		initKeys();
		prepareEmptyKeys();
		initSpaceName(tEntityAnnotation);
		initSpaceId();
		initHandlers();
		initProperties();

	}

	private void initKeys() throws Exception {
		for (java.lang.reflect.Field field : entityClass.getDeclaredFields()) {
			io.actionpay.jtom.annotations.Field tarantoolField = field.getDeclaredAnnotation(io.actionpay.jtom.annotations.Field.class);
			Key key = field.getDeclaredAnnotation(Key.class);
			if (key != null && tarantoolField == null)
				throw new UndeclaredKeyFieldException("Key field " + field.getName()
						+ " should have annotation @" + io.actionpay.jtom.annotations.Field.class.getName());
			fieldStore(tarantoolField, field);
			initSerializersStore(field);
			keyStore(key, field);
		}
	}

	private void initSerializersStore(java.lang.reflect.Field field) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		if (!field.isAnnotationPresent(Serializable.class))
			return;
		Serializable serializable = field.getAnnotation(Serializable.class);
		serializableClasses.add(field.getDeclaringClass());
		serializeManager.put(field, (Serializer) serializable.value().getMethod("instance").invoke(null));
	}

	private void initSpaceName(Entity entity) {
		space = entity.space();
	}

	private void initHandlers() throws Exception {
		for (Method method : entityClass.getDeclaredMethods())
			for (Class<? extends Annotation> obj : handlers) {
				if (method.isAnnotationPresent(obj)) {
					if (!Modifier.isStatic(method.getModifiers()))
						throw new NonStaticMethodException("Handler method `" + method.getName() + "` should be static.");
					if (method.getParameterTypes().length != 1)
						throw new WrongNumberArgsException("Handler method `" + method.getName() + "` should be have 1 argument.");
					registerHandler(obj, method);
				}
			}
	}

	protected TarantoolImpl(Class<? extends T> entityClass) throws Exception {
		this.entityClass = entityClass;
		init();
	}

	private void indexStore(Indexes indexes) throws Exception {
		int i = 0;
		for (Index index : indexes.value()) {
			if (indexPositions.containsKey(index.name()))
				throw new DuplicateIndexKeyException("Duplicate Index Key: " + index.name());
			indexPositions.put(index.name(), i);
			indexMap.put(i++, index);
		}
	}

	public QueryResult<T> find(int index, Object value) {
		return find(index, value, Integer.MAX_VALUE);
	}

	public QueryResult<T> find(int index, Object value, int limit) {
		return find(index, value, limit, 0);
	}

	public QueryResult<T> find(int index, Object value, Integer limit, Integer offset) {
		List result = ((TarantoolConnection) link).select(spaceId, index, value, offset, limit, 0);
		return new TarantoolQueryResult<>(entityClass, result);
	}

	public QueryResult<T> all() throws Exception {
		callHandler(BeforeGet.class, this, null);
		TarantoolQueryResult<T> result = new TarantoolQueryResult<>(entityClass, ((TarantoolConnection) link)
				.select(spaceId, 0, emptyKey, 0, Integer.MAX_VALUE, 2));
		callHandler(AfterGet.class, this, result);
		return result;
	}


	String buildSpaceCreateEvalExpression() throws Exception {
		StringBuilder query = new StringBuilder();
		query.append("box.schema.space.create('").append(space).append("')\n");
		for (Integer indexId : indexMap.keySet())
			query.append(buildSpaceIndexCreateEvalExpression(indexId, space, false));
		return query.toString();
	}

	private String buildSpaceIndexCreateEvalExpression(Integer indexId, String space, boolean isPacket) throws WrongTarantoolKeyTypeException {
		StringBuilder query = new StringBuilder();
		Index index = indexMap.get(indexId);
		query.append("box.space.")
				.append(space)
				.append(":create_index('")
				.append(null == index.name() ? indexId : index.name())
				.append("', {type = '").append(index.indexType().toString()).append("', ")
				.append("unique = ").append(String.valueOf(index.unique())).append(" , parts = {");
		int idInc = 1;
		for (Integer id : keys.get(indexId).keySet()) {
			java.lang.reflect.Field field = keys.get(indexId).get(id);
			if (isPacket)
				query.append(idInc++).append(", '").append(typeToKeyType(field.getType())).append("', ");
			else
				query.append(fieldPositions.get(field) + 1).append(", '").append(typeToKeyType(field.getType())).append("', ");
		}
		query.append("}})\n");

		return query.toString();
	}

	/**
	 * Create space for Entity and try to set space Id
	 *
	 * @return empty QueryResult if all is ok
	 * @throws Exception
	 */
	@Override
	public QueryResult<T> createSpace() throws Exception {
		QueryResult<T> result = new TarantoolQueryResult<>(entityClass, ((TarantoolConnection) link)
				.eval(buildSpaceCreateEvalExpression()));
		initSpaceId();
		return result;
	}

	@Override
	public QueryResult<T> dropSpace() throws Exception {
		return new TarantoolQueryResult<>(entityClass, ((TarantoolConnection) link)
				.eval("box.space." + space + ":drop()"));
	}

	@Override
	public QueryResult<T> dropById(Object id) throws Exception {

		QueryResult<T> result = (QueryResult<T>) callHandler(BeforeDrop.class, this, new TarantoolQueryResult<>(entityClass, Arrays.asList(id)));
		result = new TarantoolQueryResult<>(entityClass, ((TarantoolConnection) link)
				.delete(spaceId, result.getAsPlainList()));
		callHandler(AfterDrop.class, this, result);
		return result;
	}

	@Override
	public QueryResult<T> getById(Object id) throws Exception {
		return get(id);
	}

	@Override
	public T one(Object key) throws Exception {
		List<T> objects = get(key).getAsObjectList();
		if (objects.size() > 1)
			throw new IndexRelationException("Too many objects");
		if (objects.size() < 1)
			throw new IndexRelationException("Object not found");
		return objects.get(0);
	}

	@Override
	public List<T> many(String indexName, Object object) throws Exception {
		DAO dao2 = DAOPool.by(object.getClass());
		List indexValue = dao2.indexToList(0, object);
		return get(indexPositions.get(indexName), indexValue).getAsObjectList();
	}

	public QueryResult<T> get(Object key) throws Exception {
		return get(0, key);
	}


	public QueryResult<T> get(Integer index, Object key) throws Exception {
		QueryResult<T> result = (QueryResult<T>) callHandler(BeforeGet.class, this, new TarantoolQueryResult<>(entityClass, Arrays.asList(index, key)));
		if (!(result.getAsPlainList().get(0) == null || result.getAsPlainList().get(0) instanceof Integer))
			throw new WrongResultTypeException("Wrong index returned by handler. Handler should return QueryResult with index and key");
		index = (Integer) result.getAsPlainList().get(0);
		key = result.getAsPlainList().get(1);
		if (index == null)
			result = find(0, Collections.singletonList(key));
		else if (!(key instanceof List)) {
			result = find(index, Collections.singletonList(key));
		} else
			result = find(index, key);
		callHandler(AfterGet.class, this, result);
		return result;
	}

	private List entityToList(T entity) throws IllegalAccessException {
		List<Object> data = new ArrayList<>();
		for (int i = 0; i < fieldsCount; i++)
			if (fields.containsKey(i))
				data.add(serializeManager.marshal(fields.get(i), fields.get(i).get(entity)));
			else
				data.add(null);
		return data;
	}

	public List indexToList(String name, T entity) throws Exception {
		return indexToList(indexPositions.get(name), entity);
	}

	public List indexToList(Integer index, T entity) throws Exception {
		List<Object> data = new ArrayList<>();
		Map<Integer, java.lang.reflect.Field> keysMap = keys.get(index);
		keysMap.keySet().stream().sorted().forEach(key -> {
			try {
				java.lang.reflect.Field field = keysMap.get(key);
				data.add(serializeManager.marshal(field, field.get(entity)));
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		});

		return data;
	}

	@Override
	public <T2> void setProperty(T entity, String propertyName, T2 value, Class<T2> propertyClass) throws Exception {
		List data = indexToList(0, entity);
		Object serializedValue = serializeManager.marshal(propertyClass, value);
		if (serializedValue instanceof List)
			data.addAll((List)serializedValue);
		else
			data.add(serializedValue);
		TarantoolConnection connection = ((TarantoolConnection) propertyMapConnection.get(propertyName));
		connection.replace(propertyMapSpace.get(propertyName), data);
	}

	@Override
	public <T2> T2 getProperty(T entity, String propertyName, Class<T2> propertyClass) throws Exception {

		TarantoolConnection connection = ((TarantoolConnection) propertyMapConnection.get(propertyName));
		List data = (List) connection.select(propertyMapSpace.get(propertyName), 0, indexToList(0, entity), 0, 1, 2).get(0);
		List result = data.subList(keys.get(0).size(), data.size());
		if (serializeManager.containsKey(propertyClass))
			return (T2) serializeManager.unmarshal(propertyClass, result);
		else
			return (T2) (result.get(0));
	}


	String typeToKeyType(Class<?> type) throws WrongTarantoolKeyTypeException {
		if (Number.class.isAssignableFrom(type) || type.equals(double.class))
			return "NUM";
		if (String.class.isAssignableFrom(type))
			return "STR";
		if (List.class.isAssignableFrom(type))
			return "ARRAY";
		throw new WrongTarantoolKeyTypeException();

	}

	private static Number narrovingNumberConversion(Class<?> outputType, Number value) throws Exception {
		if (value == null)
			return null;

		if (Byte.class.equals(outputType))
			return value.byteValue();

		if (Short.class.equals(outputType))
			return value.shortValue();

		if (Integer.class.equals(outputType))
			return value.intValue();

		if (Long.class.equals(outputType))
			return value.longValue();

		if (Float.class.equals(outputType))
			return value.floatValue();

		if (Double.class.equals(outputType))
			return value.doubleValue();

		throw new NumberFormatException("Output type is not number type");

	}

	public List<T> convertPlainListToObjectList(List objects) throws Exception {
		List<T> result = new ArrayList<>();
		for (Object obj : objects) {
			T instance = entityClass.getConstructor().newInstance();
			for (java.lang.reflect.Field field : fields.values()) {

				Object fieldValue = (((List) (obj)).get(field.getDeclaredAnnotation(io.actionpay.jtom.annotations.Field.class).position()));
				if (serializeManager.containsKey(field))
					fieldValue = serializeManager.unmarshal(field, fieldValue);
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
		callHandler(BeforeAdd.class, this, entity);
		TarantoolQueryResult<T> result = new TarantoolQueryResult<>(entityClass, ((TarantoolConnection) link)
				.insert(spaceId, entityToList(entity)));
		callHandler(AfterSave.class, this, result);
		return result;
	}

	public QueryResult<T> save(T entity) throws Exception {
		callHandler(BeforeSave.class, this, entity);
		TarantoolQueryResult<T> result = new TarantoolQueryResult<>(entityClass, ((TarantoolConnection) link)
				.replace(spaceId, entityToList(entity)));
		callHandler(AfterSave.class, this, result);
		return result;
	}

	public QueryResult<T> drop(T entity) throws Exception {
		callHandler(BeforeDrop.class, this, entity);
		TarantoolQueryResult<T> result = new TarantoolQueryResult<>(entityClass, ((TarantoolConnection) link)
				.delete(spaceId, indexToList(0, entity)));
		callHandler(AfterDrop.class, this, result);
		return result;
	}


	public void setLink(Connection link) {
		this.link = link;
	}

	@Override
	public String toString() {
		return super.toString() + "{" +
				"\nentityClass=" + entityClass +
				",\n fields=" + fields +
				",\n fieldSerializers=" + serializeManager +
				",\n fieldsCount=" + fieldsCount +
				",\n keys=" + keys +
				",\n indexMap=" + indexMap +
				",\n indexPositions=" + indexPositions +
				",\n link=" + link +
				",\n space='" + space + '\'' +
				",\n spaceId=" + spaceId +
				",\n emptyKey=" + emptyKey +
				",\n handlers=" + handlers +
				'}';
	}
}


