package org.adonweb.odm;

/**
 * Connection info contain information for @see ConnectionPool to create connection
 * @author Artur Khakimov <djion@ya.ru>
 */
public class ConnectionInfo {
    private Class<? extends Connection> connectionClass;
    private String name;
    private String host;
    private Integer port;
    private String user;
    private String password;

    /**
     * Connection info constructor
     * @param connectionClass Class of connection we will create with this connection info
     * @param name Name of connection to have access to it
     * @param host connection host
     * @param port connection port
     * @param user auth username
     * @param password auth password
     */
    public ConnectionInfo(Class<? extends Connection> connectionClass, String name, String host, Integer port, String user, String password) {
        this.connectionClass = connectionClass;
        this.name = name;
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;
    }

    /**
     * Get connection class
     * @return connection class, can be used to create reflection connection
     */
    public Class<? extends Connection> getConnectionClass() {
        return connectionClass;
    }

    /**
     * @param connectionClass connection class
     * @return ConnectionInfo this object
     */
    public ConnectionInfo setConnectionClass(Class<? extends Connection> connectionClass) {
        this.connectionClass = connectionClass;
        return this;
    }

    /**
     * Connection name getter
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Connection host getter
     * @return host
     */
    public String getHost() {
        return host;
    }

    /**
     * Connection port getter
     * @return port
     */
    public Integer getPort() {
        return port;
    }

    /**
     * Connection user getter
     * @return user
     */
    public String getUser() {
        return user;
    }

    /**
     * Connection password getter
     * @return password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Connection name setter
     * @return this
     */
    public ConnectionInfo setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Connection host setter
     * @return this
     */
    public ConnectionInfo setHost(String host) {
        this.host = host;
        return this;
    }

    /**
     * Connection port setter
     * @return this
     */
    public ConnectionInfo setPort(Integer port) {
        this.port = port;
        return this;
    }

    /**
     * Connection user setter
     * @return this
     */
    public ConnectionInfo setUser(String user) {
        this.user = user;
        return this;
    }

    /**
     * Connection password setter
     * @return this
     */
    public ConnectionInfo setPassword(String password) {
        this.password = password;
        return this;
    }


}
