package net.actionpay.jtom.tarantool;

import net.actionpay.jtom.Connection;
import org.tarantool.TarantoolConnection16Impl;

import java.io.IOException;
import java.util.List;

/**
 * Wrapper for tarantool connection based on 1.6 version of tarantool connector
 *
 * @author Artur Khakimov <djion@ya.ru>
 */
public class TarantoolConnection implements Connection {

    TarantoolConnection16Impl connection;

    @Override
    public void connect(String host, int port, String user, String password) throws IOException {
        connection = new TarantoolConnection16Impl(host, port);
        connection.auth(user, password);
    }

    @Override
    public void ping() throws Exception {
        if (!connection.ping())
            throw new Exception("Ping failed");
    }

    @Override
    public void disconnect() throws Exception {
        connection.close();
    }

    public List eval(String eval) {
        return connection.eval(eval);
    }

    public List select(int spaceId, int index, Object value, int offset, int limit, int iterator) {
        return connection.select(spaceId, index, value, offset, limit, iterator);
    }

    public List insert(Integer spaceId, List list) {
        return connection.insert(spaceId, list);
    }

    public List replace(Integer spaceId, List list) {
        return connection.replace(spaceId, list);
    }

    public List delete(Integer spaceId, List list) {
        return connection.delete(spaceId, list);
    }
}
