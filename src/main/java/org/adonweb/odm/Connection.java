package org.adonweb.odm;

/**
 *
 * @author Artur Khakimov <djion@ya.ru>
 */
public interface Connection {
    void connect(String host, int port, String user, String password) throws Exception;
    void ping() throws Exception;
    void disconnect() throws Exception;
}
