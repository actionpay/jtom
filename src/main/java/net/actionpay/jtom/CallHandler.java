package net.actionpay.jtom;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author Artur Khakimov <djion@ya.ru>
 */
public interface CallHandler {

    /**
     * Add method for handler
     *
     * @param handler using this object you can activate handler
     * @param method method should be activate by this handler
     */
    public void registerHandler(Object handler, Method method);

    /**
     * Call methods registred for this handler
     *
     * @param handler trigger source
     * @param source this object will be used for calling method. Use null for static
     * @param args arguments for method
     * @return result of call
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public Object callHandler(Object handler, Object source, Object args) throws Exception;


}

