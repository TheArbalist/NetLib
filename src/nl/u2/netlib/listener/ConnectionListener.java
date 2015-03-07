package nl.u2.netlib.listener;

import nl.u2.netlib.listener.event.ConnectionDestroyedEvent;
import nl.u2.netlib.listener.event.ConnectionEstablishedEvent;
import nl.u2.netlib.listener.event.PacketReceivedEvent;

public interface ConnectionListener {

	void onConnectionEstablished(ConnectionEstablishedEvent event);
	
	void onConnectionDestroyed(ConnectionDestroyedEvent event);
	
	void onPacketReceived(PacketReceivedEvent event);
	
	void onExceptionThrown(Throwable t);
	
}
