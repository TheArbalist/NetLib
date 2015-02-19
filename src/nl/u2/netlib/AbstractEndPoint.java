package nl.u2.netlib;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import nl.u2.netlib.event.SessionConnectedEvent;
import nl.u2.netlib.event.SessionDisconnectedEvent;
import nl.u2.netlib.event.SessionReceivedEvent;

public class AbstractEndPoint implements EndPoint {

	protected final List<SessionListener> listeners = new ArrayList<SessionListener>();

	public void addListener(SessionListener listener) {
		synchronized(listeners) {
			listeners.add(listener);
		}
	}
	
	public void removeListener(SessionListener listener) {
		synchronized(listeners) {
			listeners.remove(listener);
		}
	}
	
	public void fireSessionReceived(Session session, TransmissionProtocol protocol, ByteBuffer buffer) {
		SessionReceivedEvent event = new SessionReceivedEvent(session, protocol, buffer);
		
		synchronized(listeners) {
			for(SessionListener listener : listeners) {
				listener.onSessionReceived(event);
			}
		}
	}
	
	public void fireSessionConnected(Session session) {
		SessionConnectedEvent event = new SessionConnectedEvent(session);
		
		synchronized(listeners) {
			for(SessionListener listener : listeners) {
				listener.onSessionConnected(event);
			}
		}
	}
	
	public void fireSessionDisconnected(Session session) {
		SessionDisconnectedEvent event = new SessionDisconnectedEvent(session);
		
		synchronized(listeners) {
			for(SessionListener listener : listeners) {
				listener.onSessionDisconnected(event);
			}
		}
	}
	
}
