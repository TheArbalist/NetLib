package nl.u2.netlib.oio;

import java.net.SocketAddress;

/**
 * <em><b>INTERNAL USE ONLY</b></em>
 */
interface OioClientChannel {

	/**
	 * <em><b>INTERNAL USE ONLY</b></em>
	 */
	void connect(SocketAddress address) throws Exception;
	
	/**
	 * <em><b>INTERNAL USE ONLY</b></em>
	 */
	void close() throws Exception;
	
	/**
	 * <em><b>INTERNAL USE ONLY</b></em>
	 */
	boolean isActive();
}
