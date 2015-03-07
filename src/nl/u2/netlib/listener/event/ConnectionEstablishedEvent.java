package nl.u2.netlib.listener.event;

import nl.u2.netlib.Connection;

public final class ConnectionEstablishedEvent extends ConnectionEvent {

	public ConnectionEstablishedEvent(Connection connection) {
		super(connection);
	}

}
