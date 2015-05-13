package org.adonweb.odm;

import org.adonweb.odm.exception.InvalidArgumentException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Artur Khakimov <djion@ya.ru>
 */
public class ConnectionPool {
    static Map<String,Connection> pool = new HashMap<String, Connection>();

    public static void init(List<ConnectionInfo> list) throws Exception {
        for(ConnectionInfo info:list) {
            establish(info.getConnectionClass(), info.getName(), info.getHost(), info.getPort(), info.getUser(), info.getPassword());
        }
    }

    protected static void establish(Class<? extends Connection> connector, String name, String host, Integer port, String user, String password) throws Exception {
        if( pool.containsKey(name) ) {
            throw new InvalidArgumentException();
        }
        Connection connection = connector.getConstructor().newInstance();
        connection.connect(host, port, user, password);
        pool.put(name,connection);
    }

    public static Connection connection(String name) throws Exception {
        if( !pool.containsKey(name) ) {
            throw new InvalidArgumentException();
        }
        return pool.get(name);
    }

    public static void done() {
        pool.values().stream().forEach(connection ->{try {connection.disconnect();} catch (Exception ignored) {}});
    }
}
