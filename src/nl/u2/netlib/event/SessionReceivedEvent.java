package nl.u2.netlib.event;

import java.nio.ByteBuffer;

import nl.u2.netlib.Session;

public class SessionReceivedEvent extends SessionEvent {

	protected ByteBuffer buffer;
	
	public SessionReceivedEvent(Session session, ByteBuffer buffer) {
		super(session);
		
		this.buffer = buffer;
	}
	
	public ByteBuffer getBuffer() {
		return buffer;
	}

}
