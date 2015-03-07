package nl.u2.netlib;

import java.util.Collection;

import nl.u2.netlib.listener.ConnectionListener;
import nl.u2.netlib.listener.event.ConnectionDestroyedEvent;
import nl.u2.netlib.listener.event.ConnectionEstablishedEvent;
import nl.u2.netlib.listener.event.PacketReceivedEvent;

/**
 * An {@link EndPoint} represents either a {@link Client} or a {@link Server}.
 * It listens for one or more {@link Connection}(s) and, depending on the implementation, it allows
 * you to connect or to bind it. An {@link EndPoint} is always closable.
 * 
 * @author U2ForMe
 *
 */
public interface EndPoint {

	/**
	 * Adds a {@link nl.u2.netlib.listener.ConnectionListener connection listener} that will be notified of various events
	 * such as incoming {@link Connection}s and 
	 * {@link nl.u2.netlib.packet.Packet Packet}s.
	 * 
	 * @param listener
	 * 			the {@link nl.u2.netlib.listener.ConnectionListener connection listener} to add to this {@link EndPoint}
	 */
	void addConnectionListener(ConnectionListener listener);
	
	/**
	 * Removes a {@link nl.u2.netlib.listener.ConnectionListener connection listener}
	 * .
	 * @param listener
	 * 			the {@link nl.u2.netlib.listener.ConnectionListener connection listener} to remove from {@link EndPoint}
	 */
	void removeConnectionListener(ConnectionListener listener);
	
	/**
	 * Returns all {@link nl.u2.netlib.listener.ConnectionListener connection listeners},
	 * this should only be used for reading and not to write or remove from.
	 * 
	 * @return 
	 * 		a {@link Collection} of all currently added {@link nl.u2.netlib.listener.ConnectionListener connection listeners}
	 */
	Collection<ConnectionListener> getConnectionListeners();
	
	/**
	 * Invokes a connection established event on all {@link nl.u2.netlib.listener.ConnectionListener connection listeners}.
	 * 
	 * @param event
	 * 			The event to be invoked on all listeners
	 */
	void fireConnectionEstablished(ConnectionEstablishedEvent event);
	
	/**
	 * Invokes a connection destroyed event on all {@link nl.u2.netlib.listener.ConnectionListener connection listeners}.
	 * 
	 * @param event
	 * 			The event to be invoked on all listeners
	 */
	void fireConnectionDestroyed(ConnectionDestroyedEvent event);
	
	/**
	 * Invokes a connection established event on all {@link nl.u2.netlib.listener.ConnectionListener connection listener}s.
	 * 
	 * @param event
	 */
	void firePacketReceived(PacketReceivedEvent event);
	
	/**
	 * Invokes an exception thrown event on all {@link nl.u2.netlib.listener.ConnectionListener connection listener}s.
	 * 
	 * @param t
	 * 		The throwable to be invoked on all listeners
	 */
	void fireExceptionThrown(Throwable t);
	
	/**
	 * Closes this {@link EndPoint} so that it can no longer be used to receive events.
	 */
	void close();
	
	/**
	 * Returns the state of this EndPoint
	 * 
	 * @return true if, and only if this EndPoint is bound/connected.
	 */
	boolean isActive();
	
}
