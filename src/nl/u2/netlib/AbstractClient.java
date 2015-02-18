package nl.u2.netlib;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public abstract class AbstractClient extends AbstractEndPoint implements Client {

	protected abstract void handleConnect(SocketAddress tcpAddress, SocketAddress udpAddress) throws IOException;
	
	protected abstract void handleClose();
	
	protected boolean running;
	
	public void connect(String host, int tcpPort, int udpPort) throws IOException {
		if(host == null || host.isEmpty() || tcpPort < 0 || udpPort < 0) {
			throw new IllegalArgumentException("connect requires a valid host, TCP and UDP port.");
		}
		
		if(running) {
			close();
		}
		
		handleConnect(new InetSocketAddress(host, tcpPort), new InetSocketAddress(host, udpPort));
		running = true;
	}
	
	public void close() {
		handleClose();
		running = false;
	}
	
}
