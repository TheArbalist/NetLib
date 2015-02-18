package nl.u2.netlib;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public abstract class AbstractServer extends AbstractEndPoint implements Server {

	protected abstract void handleBind(SocketAddress tcpAddress, SocketAddress udpAddress) throws IOException;
	
	protected abstract void handleClose();
	
	protected boolean running;
	
	public void bind(int tcpPort, int udpPort) throws IOException {
		if(tcpPort < 0 || udpPort < 0) {
			throw new IllegalArgumentException("Bind requires a valid TCP and UDP port.");
		}
		
		if(running) {
			close();
		}
		
		handleBind(new InetSocketAddress(tcpPort), new InetSocketAddress(udpPort));
		running = true;
	}
	
	public void close() {
		handleClose();
		running = false;
	}
	
}
