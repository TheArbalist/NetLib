package nl.u2.netlib.nio;

import java.net.SocketAddress;
import java.nio.channels.Selector;

/**
 * <em><b>INTERNAL USE ONLY</b></em>
 */
interface NioServerChannel {

	/**
	 * <em><b>INTERNAL USE ONLY</b></em>
	 */
	void bind(Selector selector, SocketAddress address) throws Exception;
	
	/**
	 * <em><b>INTERNAL USE ONLY</b></em>
	 */
	void close() throws Exception;
	
	/**
	 * <em><b>INTERNAL USE ONLY</b></em>
	 */
	boolean isActive();
}
