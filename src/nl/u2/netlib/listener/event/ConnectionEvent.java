package nl.u2.netlib.listener.event;

import nl.u2.netlib.Connection;

public class ConnectionEvent extends Event {

	private Connection connection;
	
	protected ConnectionEvent(Connection connection) {
		this.connection = connection;
	}
	
	public Connection connection() {
		return connection;
	}
	
}
