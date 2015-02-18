package nl.u2.netlib.event;

import nl.u2.netlib.Session;

public class SessionDisconnectedEvent extends SessionEvent {

	public SessionDisconnectedEvent(Session session) {
		super(session);
	}

}
