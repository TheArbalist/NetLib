package nl.u2.netlib.listener.event;

import nl.u2.netlib.Connection;

public final class ConnectionDestroyedEvent extends ConnectionEvent {

	public ConnectionDestroyedEvent(Connection connection) {
		super(connection);
	}

}
