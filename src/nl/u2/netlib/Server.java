package nl.u2.netlib;

/**
 * A {@link Server} is an {@link EndPoint} which is bindable. It listens for connections.
 * Note that UDP {@link Server} implementations often also implement {@link Pipeline}.
 * 
 * @author U2ForMe
 *
 */
public interface Server extends EndPoint {

	/**
	 * Binds this Server to a specified TCP and UDP port. When invoking this method, the {@link EndPoint#close()} method
	 * will be called first.
	 * @param tcpPort
	 * 			The TCP port to bind to.
	 * @param udpPort
	 * 			The UDP port to bind to.
	 * @throws Exception
	 * 			When: 1. Given TCP port is invalid;
	 * 				  2. Given UDP port is invalid;
	 * 				  3. Couldn't bind to any of the given ports.
	 * 			
	 */
	void bind(int tcpPort, int udpPort) throws Exception;
	
}
