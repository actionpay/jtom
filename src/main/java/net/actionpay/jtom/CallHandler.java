package net.actionpay.jtom;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Artur Khakimov <djion@ya.ru>
 */
public class CallHandler {
    HashMap<Object, List<Method>> map = new HashMap<>();

    /**
     * Add method for handler
     *
     * @param handler using this object you can activate handler
     * @param method method should be activate by this handler
     */
    public void registerHandler(Object handler, Method method) {
        if (!map.containsKey(handler))
            map.put(handler, new ArrayList<>());
        map.get(handler).add(method);
    }

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
    public Object callHandler(Object handler, Object source, Object args) throws InvocationTargetException, IllegalAccessException {
        Object result = args;
        if (map.containsKey(handler))
            for (Method method : map.get(handler))
                result = method.invoke(source, result);

        return result;
    }


}

