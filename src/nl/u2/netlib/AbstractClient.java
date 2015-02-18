package nl.u2.netlib;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractClient extends AbstractEndPoint implements Client {

	protected abstract void handleConnect(SocketAddress tcpAddress, SocketAddress udpAddress) throws IOException;
	
	protected abstract void handleClose();
	
	private AtomicBoolean running = new AtomicBoolean(false);
	
	public void connect(String host, int tcpPort, int udpPort) throws IOException {
		if(host == null || host.isEmpty() || tcpPort < 0 || udpPort < 0) {
			throw new IllegalArgumentException("connect requires a valid host, TCP and UDP port.");
		}
		
		if(running.getAndSet(true)) {
			close();
		}
		
		try {
			handleConnect(new InetSocketAddress(host, tcpPort), new InetSocketAddress(host, udpPort));
		} catch(IOException e) {
			running.set(false);
			throw e;
		}
	}
	
	public void close() {
		if(running.getAndSet(false)) {
			handleClose();
		}
	}
	
	public boolean isConnected() {
		return running.get();
	}
	
}
