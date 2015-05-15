package net.actionpay.jtom;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Temp on 15.05.2015.
 */
public class EntityDaoHandler {
    HashMap<Object, List<Method>> map = new HashMap<>();

    public void registerHandler(Object handler, Method method){
        if (!map.containsKey(handler))
            map.put(handler, new ArrayList<>());
        map.get(handler).add(method);
    }

    public void callHandler(Object handler, Object source, Object... args) throws InvocationTargetException, IllegalAccessException {
        if (map.containsKey(handler))
        for (Method method : map.get(handler))
        if (args.length>0)
            method.invoke(source, args);
        else
            method.invoke((Object)source);
    }


}

