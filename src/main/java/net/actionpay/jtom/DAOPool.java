package net.actionpay.jtom;

import net.actionpay.jtom.annotations.Entity;

import java.util.HashMap;

/**
 * @author Artur Khakimov <djion@ya.ru>
 */
public class DAOPool {
    static HashMap<Class<?>,DAO> daoPool = new HashMap<>();
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
    static DAO buildDaoByEntityClass(Class<?> clazz) throws Exception {

        return (DAO)ConnectionPool.connection(clazz.getDeclaredAnnotation(Entity.class).connection()).daoClass()
                .getMethod("getByClass", Class.class).invoke(null, clazz);
    }

    public static DAO by(Class<?> clazz) throws Exception {
        daoPool.putIfAbsent(clazz, buildDaoByEntityClass(clazz));
        return daoPool.get(clazz);
    }
}
