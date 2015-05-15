package net.actionpay.jtom;

/**
 * Connection is link between your application and some remote or local resource
 *
 * @author Artur Khakimov <djion@ya.ru>
 */
public interface Connection {
    /**
     * Create connection
     *
     * @param host     connection host
     * @param port     connection port
     * @param user     auth username
     * @param password auth password
     * @throws Exception some connections can throw exceptions with some errors, you know
     */
    void connect(String host, int port, String user, String password) throws Exception;

    /**
     * Ping connection
     *
     * @throws Exception if connection is not pinged throw exception
     */
    void ping() throws Exception;

    /**
     * Close connection
     *
     * @throws Exception sometimes it's to hard to close closed connections or can be some errors during proccess
     */
    void disconnect() throws Exception;

    default Class<?> daoClass(){return null;};
}
