package nl.u2.netlib.event;

import nl.u2.netlib.Session;

public class SessionExceptionEvent extends SessionEvent {

	private Throwable cause;
	
	public SessionExceptionEvent(Session session, Throwable cause) {
		super(session);
		this.cause = cause;
	}
	
	public Throwable getCause() {
		return cause;
	}

}
