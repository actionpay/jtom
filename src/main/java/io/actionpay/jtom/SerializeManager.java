package io.actionpay.jtom;

import java.util.HashMap;

/**
 * @author Artur Khakimov <djion@ya.ru>
 */
public class SerializeManager extends HashMap<Object, Serializer>{
	static SerializeManager instance = null;

	Serializer simpleSerializer = new Serializer() {
		@Override
		public Object marshal(Object o) {
			return o;
		}

		@Override
		public Object unmarshal(Object o) {
			return o;
		}
	};

	public Object marshal(Object key, Object object) {
		return getOrDefault(key, simpleSerializer).marshal(object);
	}

	public Object unmarshal(Object key, Object object) {
		return getOrDefault(key, simpleSerializer).unmarshal(object);
	}

	public static SerializeManager getInstance() {
		if (instance == null)
			instance = new SerializeManager();
		return instance;
	}

	public static void register(Object key, Serializer serializer) {
		getInstance().put(key,serializer);
	}
}
