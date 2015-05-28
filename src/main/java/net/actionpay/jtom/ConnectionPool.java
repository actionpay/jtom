package net.actionpay.jtom;

import net.actionpay.jtom.exception.InvalidArgumentException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Store connection pool for DAO
 *
 * @author Artur Khakimov <djion@ya.ru>
 */
public class ConnectionPool {
	static Map<String, Connection> pool = new HashMap<>();

	/**
	 * Establish connection pool
	 *
	 * @param list list of connections to establish
	 * @throws Exception
	 */
	public static void init(List<ConnectionInfo> list) throws Exception {
		for (ConnectionInfo info : list) {
			establish(info);
		}
	}

	/**
	 * Establish connection for info
	 *
	 * @param info connection to establish
	 * @throws Exception
	 */
	protected static void establish(ConnectionInfo info) throws Exception {
		establish(info.getConnectionClass(), info.getName()
				, info.getHost(), info.getPort(), info.getUser(), info.getPassword());
	}

	/**
	 * @param connector class of connector
	 * @param name      connection name
	 * @param host      connection host
	 * @param port      connection port
	 * @param user      auth username
	 * @param password  auth password
	 * @throws Exception
	 */
	protected static void establish(Class<? extends Connection> connector
			, String name, String host, Integer port, String user, String password) throws Exception {
		if (pool.containsKey(name)) {
			throw new InvalidArgumentException("Pool already contains connection: " + name);
		}
		Connection connection = connector.getConstructor().newInstance();
		connection.connect(host, port, user, password);
		pool.put(name, connection);
	}

	/**
	 * Get connection by name
	 *
	 * @param name name of connection
	 * @return connection if exist or throw exception if not
	 * @throws Exception
	 */
	public static Connection connection(String name) throws Exception {
		if (!pool.containsKey(name)) {
			throw new InvalidArgumentException("`" + name + "` connection is not exist.");
		}
		return pool.get(name);
	}

	/**
	 * Close all connections
	 */
	public static void done() {
		pool.values().stream().forEach(connection -> {
			try {
				connection.disconnect();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		});
		pool.clear();
	}
}
