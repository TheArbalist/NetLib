package nl.u2.netlib.listener;

import nl.u2.netlib.listener.event.ConnectionDestroyedEvent;
import nl.u2.netlib.listener.event.ConnectionEstablishedEvent;
import nl.u2.netlib.listener.event.PacketReceivedEvent;

public class ConnectionListenerAdapter implements ConnectionListener {

	public void onConnectionEstablished(ConnectionEstablishedEvent event) {
	}

	public void onConnectionDestroyed(ConnectionDestroyedEvent event) {
	}

	public void onPacketReceived(PacketReceivedEvent event) {
	}
	
	public void onExceptionThrown(Throwable t) {
	}

}
