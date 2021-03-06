package nl.u2.netlib;

/**
 * A {@link Client} is an {@link EndPoint} which is connectable. Because a {@link Client} typically
 * represents a {@link Connection}, it also implements {@link Connection}.
 * 
 * @author U2ForMe
 *
 */
public interface Client extends EndPoint, Connection {

	/**
	 * Connects this Client to a specified host and given ports.
	 * @param host
	 * 			The host to connect to.
	 * @param tcpPort
	 * 			The TCP port to connect to.
	 * @param udpPort
	 * 			The UDP port to connect to.
	 * @throws Exception
	 * 			When: 1. Given host is invalid;
	 * 				  2. Given TCP port is invalid;
	 * 				  3. Given UDP port is invalid;
	 * 				  4. Couldn't connect to the remote address.
	 */
	void connect(String host, int tcpPort, int udpPort) throws Exception;
	
}
