package io.actionpay.jtom;

import io.actionpay.jtom.annotations.Entity;

import java.util.HashMap;

/**
 * Dao pool
 *
 * @author Artur Khakimov <djion@ya.ru>
 */
public class DAOPool {
	static HashMap<Class<?>, DAO> daoPool = new HashMap<>();

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
	static DAO buildDaoByEntityClass(Class<?> clazz) throws Exception {
		return (DAO) ConnectionPool.connection(clazz.getDeclaredAnnotation(Entity.class).connection()).daoClass()
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
