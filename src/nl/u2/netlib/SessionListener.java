package nl.u2.netlib;

import nl.u2.netlib.event.SessionConnectedEvent;
import nl.u2.netlib.event.SessionDisconnectedEvent;
import nl.u2.netlib.event.SessionExceptionEvent;
import nl.u2.netlib.event.SessionReceivedEvent;

public interface SessionListener {

	public void onSessionConnected(SessionConnectedEvent event);
	
	public void onSessionDisconnected(SessionDisconnectedEvent event);
	
	public void onSessionReceived(SessionReceivedEvent event);
	
	public void onExceptionThrown(SessionExceptionEvent event);
	
}
