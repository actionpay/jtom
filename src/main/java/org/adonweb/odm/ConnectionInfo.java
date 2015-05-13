package org.adonweb.odm;

/**
 * @author Artur Khakimov <djion@ya.ru>
 */
public class ConnectionInfo {
    Class<? extends Connection> connectionClass;
    private String name;
    private String host;
    private Integer port;
    private String user;
    private String password;

    public ConnectionInfo(Class<? extends Connection> connectionClass, String name, String host, Integer port, String user, String password) {
        this.connectionClass = connectionClass;
        this.name = name;
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;
    }

    public Class<? extends Connection> getConnectionClass() {
        return connectionClass;
    }

    public ConnectionInfo setConnectionClass(Class<? extends Connection> connectionClass) {
        this.connectionClass = connectionClass;
        return this;
    }

    public String getName() {
        return name;
    }

    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public ConnectionInfo setName(String name) {
        this.name = name;
        return this;
    }

    public ConnectionInfo setHost(String host) {
        this.host = host;
        return this;
    }

    public ConnectionInfo setPort(Integer port) {
        this.port = port;
        return this;
    }

    public ConnectionInfo setUser(String user) {
        this.user = user;
        return this;
    }

    public ConnectionInfo setPassword(String password) {
        this.password = password;
        return this;
    }


}
