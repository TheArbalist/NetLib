package nl.u2.netlib.nio;

import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

final class NioTcpServer implements NioServerChannel {

	ServerSocketChannel server;
	
	public void bind(Selector selector, SocketAddress address) throws Exception {
		selector.wakeup();
		server = selector.provider().openServerSocketChannel();
		server.bind(address);
		server.configureBlocking(false);
		server.register(selector, SelectionKey.OP_ACCEPT);
	}

	public void close() throws Exception {
		if(server != null) {
			try {
				server.close();
			} catch(Exception e) {
				throw e;
			} finally {
				server = null;
			}
		}
	}
	
	public boolean isActive() {
		return server != null && server.isRegistered() && server.isOpen();
	}
}
