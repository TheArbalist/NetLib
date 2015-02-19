package nl.u2.netlib.event;

import java.nio.ByteBuffer;

import nl.u2.netlib.Session;
import nl.u2.netlib.TransmissionProtocol;

public class SessionReceivedEvent extends SessionEvent {

	protected TransmissionProtocol protocol;
	protected ByteBuffer buffer;
	
	public SessionReceivedEvent(Session session, TransmissionProtocol protocol, ByteBuffer buffer) {
		super(session);
		
		this.protocol = protocol;
		this.buffer = buffer;
	}
	
	public TransmissionProtocol protocol() {
		return protocol;
	}
	
	public ByteBuffer buffer() {
		return buffer;
	}

}
