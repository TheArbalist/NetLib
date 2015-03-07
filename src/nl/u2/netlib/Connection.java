package nl.u2.netlib;

/**
 * A {@link Connection} provides a TCP and UDP {@link Pipeline} to send and receive data from. 
 * 
 * @author U2ForMe
 * 
 */
public interface Connection {

	/**
	 * Represents the TCP {@link Pipeline} pipeline of this connection,
	 * it can be used to write data to and retrieve connection information from.
	 * This should <em>never</em> return null.
	 * 
	 * @return a {@link Pipeline} for TCP operations
	 */
	Pipeline tcp();
	
	/**
	 * Represents the UDP {@link Pipeline} pipeline of this connection,
	 * it can be used to write data to and retrieve connection information from.
	 * This should <em>never</em> return null.
	 * 
	 * @return a {@link Pipeline} for UDP operations
	 */
	Pipeline udp();
	
	/**
	 * The {@link EndPoint} to which this {@link Connection} is bound to. This should <em>never</em> return null.
	 * 
	 * @return an {@link EndPoint} to which this connection is bound to
	 */
	EndPoint endPoint();
	
	/**
	 * Closes this {@link Connection}, typically closes both {@link Pipeline}s, although there's no
	 * close method, it is often declared in a package-private way.
	 */
	void close();
	
	/**
	 * Returns the state of this {@link Connection}.
	 * @return true if, and only if this connection is connected and able to send and receive packets.
	 */
	boolean isActive();
	
}
