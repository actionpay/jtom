package net.actionpay.jtom;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Temp on 15.05.2015.
 */
public class EntityDaoHandler {
    HashMap<Object, List<Method>> map = new HashMap<>();

    public void registerHandler(Object handler, Method method) {
        if (!map.containsKey(handler))
            map.put(handler, new ArrayList<>());
        map.get(handler).add(method);
    }

    public Object callHandler(Object handler, Object source, Object args) throws InvocationTargetException, IllegalAccessException {
        Object result = args;
        if (map.containsKey(handler))
            for (Method method : map.get(handler)) {
                result = method.invoke(source,result);
            }
        return result;
    }


}

