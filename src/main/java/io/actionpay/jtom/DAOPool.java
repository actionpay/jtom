package io.actionpay.jtom;

import io.actionpay.jtom.annotations.Entity;
import io.actionpay.jtom.annotations.Properties;
import io.actionpay.jtom.annotations.Property;

import java.util.HashMap;
import java.util.Map;

/**
 * Dao pool
 *
 * @author Artur Khakimov <djion@ya.ru>
 */
public class DAOPool {
	static Map<Class<?>, DAO> daoPool = new HashMap<>();
	static Map<Class<?>, Map<String,DAO>> daoProperties = new HashMap<>();

	static {
		/**
		 * Find all annotations with Entity mark
		 */
		AnnotationScanner.find(Entity.class).forEach(cl -> {
			try {
				by(cl);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	/**
	 * Build DAO by annotated @Entity class
	 *
	 * @param clazz anotated @Entity class to build DAO
	 * @return DAO
	 * @throws Exception depends on DAO Engine
	 */
	static <T> DAO<T> buildDaoByEntityClass(Class<T> clazz) throws Exception {
		return (DAO<T>) ConnectionPool.connection(clazz.getDeclaredAnnotation(Entity.class).connection()).daoClass()
				.getMethod("getByClass", Class.class).invoke(null, clazz);
	}

	/**
	 * Get or build and get DAO by class annotated by @Entity
	 *
	 * @param clazz
	 * @return DAO
	 * @throws Exception depends on DAO Engine
	 */
	public static <T> DAO<T> by(Class<T> clazz) throws Exception {
		daoPool.putIfAbsent(clazz, buildDaoByEntityClass(clazz));
		return daoPool.get(clazz);
	}

}
