package nl.u2.netlib;

import java.net.InetSocketAddress;

import nl.u2.netlib.packet.Packet;

/**
 * A {@link Pipeline} typically represents the actual connection between two {@link EndPoint}s.
 * For example it contains the {@link java.net.Socket socket} of an OIO TCP {@link Client}.
 * 
 * @author U2ForMe
 *
 */
public interface Pipeline {
	
	/**
	 * Writes a packet to the connected {@link EndPoint}.
	 * 
	 * @param packet
	 * 			The packet to be written
	 */
	void write(Packet packet);
	
	/**
	 * Returns the {@link Connection} of this {@link EndPoint}.
	 * 
	 * @return
	 * 		The {@link Connection} to which this {@link Pipeline} is bound to.
	 */
	Connection connection();

	/**
	 * Returns the local address of this {@link Pipeline}, typically returns
	 * {@link java.net.Socket#getLocalSocketAddress() socket#getLocalSocketAddress()}, it is already casted to
	 * an {@link java.net.InetSocketAddress InetSocketAddress} for user-convenience.
	 * 
	 * @return
	 * 		The local address of this {@link Pipeline}
	 */
	InetSocketAddress localAddress();
	
	/**
	 * Returns the remote address of this {@link Pipeline}, typically returns
	 * {@link java.net.Socket#getRemoteSocketAddress() socket#getRemoteSocketAddress()}, it is already casted to
	 * an {@link java.net.InetSocketAddress InetSocketAddress} for user-convenience.
	 * 
	 * @return
	 * 		The remote address of this {@link Pipeline}
	 */
	InetSocketAddress remoteAddress();
	
	/**
	 * 
	 * @return
	 * 		The {@link TransmissionProtocol} for this Pipeline
	 */
	TransmissionProtocol protocol();
	
	/**
	 * Returns the state of this {@link Pipeline}.
	 * 
	 * @return
	 * 		true if, and only if this {@link Pipeline} is connected and able to write {@link Packet}s
	 * 		
	 */
	boolean isActive();
	
}
