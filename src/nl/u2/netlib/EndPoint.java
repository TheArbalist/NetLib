package nl.u2.netlib;

import java.util.Collection;

import nl.u2.netlib.listener.ConnectionListener;
import nl.u2.netlib.listener.event.ConnectionDestroyedEvent;
import nl.u2.netlib.listener.event.ConnectionEstablishedEvent;
import nl.u2.netlib.listener.event.PacketReceivedEvent;

/**
 * 
 * An {@link EndPoint} represents either a {@link Client} or a {@link Server}, 
 * it listens for one or more {@link Connection}(s) and depending on the implementation allows
 * you to connect or to bind it. An {@link EndPoint} is always closeable.
 * 
 * @author U2ForMe
 *
 */
public interface EndPoint {

	/**
	 * Adds a connection listener that will be notified of various events such as {@link Connection}s and 
	 * {@link nl.u2.netlib.packet.Packet Packets}.
	 * @param listener
	 */
	void addConnectionListener(ConnectionListener listener);
	
	/**
	 * Removes a connection listener.
	 * @param listener
	 */
	void removeConnectionListener(ConnectionListener listener);
	
	/**
	 * Returns all listeners, this should only be used for reading and not to write or remove from.
	 * @return a {@link Collection} of all currently added {@link ConnectionListener}s.
	 */
	Collection<ConnectionListener> getConnectionListeners();
	
	/**
	 * Invokes a connection established event on all listeners.
	 * @param event
	 */
	void fireConnectionEstablished(ConnectionEstablishedEvent event);
	
	/**
	 * Invokes a connection destroyed event on all listeners.
	 * @param event
	 */
	void fireConnectionDestroyed(ConnectionDestroyedEvent event);
	
	/**
	 * Invokes a connection established event on all listeners.
	 * @param event
	 */
	void firePacketReceived(PacketReceivedEvent event);
	
	/**
	 * Invokes an exception thrown event on all listeners.
	 * @param t
	 */
	void fireExceptionThrown(Throwable t);
	
	/**
	 * Closes this EndPoint so that it can no longer be used to accept connections and receive events.
	 */
	void close();
	
	/**
	 * Returns the state of this EndPoint
	 * @return <code>true</code> if, and only if this EndPoint is bound/connected.
	 */
	boolean isActive();
	
}
