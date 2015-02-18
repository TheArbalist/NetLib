package nl.u2.netlib.event;

import nl.u2.netlib.Session;

public class SessionConnectedEvent extends SessionEvent {

	public SessionConnectedEvent(Session session) {
		super(session);
	}

}
