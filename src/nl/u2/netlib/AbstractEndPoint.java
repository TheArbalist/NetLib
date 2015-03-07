package nl.u2.netlib;

import java.util.ArrayList;
import java.util.Collection;

import nl.u2.netlib.listener.ConnectionListener;
import nl.u2.netlib.listener.event.ConnectionDestroyedEvent;
import nl.u2.netlib.listener.event.ConnectionEstablishedEvent;
import nl.u2.netlib.listener.event.PacketReceivedEvent;

public abstract class AbstractEndPoint implements EndPoint {

	private final Collection<ConnectionListener> listeners = new ArrayList<ConnectionListener>();
	
	public void addConnectionListener(ConnectionListener listener) {
		synchronized(listeners) {
			listeners.add(listener);
		}
	}

	public void removeConnectionListener(ConnectionListener listener) {
		synchronized(listeners) {
			listeners.remove(listener);
		}
	}

	public Collection<ConnectionListener> getConnectionListeners() {
		synchronized(listeners) {
			return listeners;
		}
	}

	public void fireConnectionEstablished(ConnectionEstablishedEvent event) {
		synchronized(listeners) {
			for(ConnectionListener listener : listeners) {
				listener.onConnectionEstablished(event);
			}
		}
	}

	public void fireConnectionDestroyed(ConnectionDestroyedEvent event) {
		synchronized(listeners) {
			for(ConnectionListener listener : listeners) {
				listener.onConnectionDestroyed(event);
			}
		}
	}

	public void firePacketReceived(PacketReceivedEvent event) {
		synchronized(listeners) {
			for(ConnectionListener listener : listeners) {
				listener.onPacketReceived(event);
			}
		}
	}
	
	public void fireExceptionThrown(Throwable t) {
		synchronized(listeners) {
			for(ConnectionListener listener : listeners) {
				listener.onExceptionThrown(t);
			}
		}
	}

}
