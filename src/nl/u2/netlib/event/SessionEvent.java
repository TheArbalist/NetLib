package nl.u2.netlib.event;

import nl.u2.netlib.Session;

public class SessionEvent {

	protected Session session;
	
	public SessionEvent(Session session) {
		this.session = session;
	}
	
	public Session getSession() {
		return session;
	}
	
}
