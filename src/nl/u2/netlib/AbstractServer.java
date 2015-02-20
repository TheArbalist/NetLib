package nl.u2.netlib;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractServer extends AbstractEndPoint implements Server {

	protected abstract void handleBind(SocketAddress tcpAddress, SocketAddress udpAddress) throws IOException;
	
	protected abstract void handleClose();
	
	private AtomicBoolean running = new AtomicBoolean(false);
	
	public void bind(int tcpPort, int udpPort) {
		if(tcpPort < 0 || udpPort < 0) {
			throw new IllegalArgumentException("Bind requires a valid TCP and UDP port.");
		}
		
		if(running.getAndSet(true)) {
			close();
		}
		
		try {
			handleBind(new InetSocketAddress(tcpPort), new InetSocketAddress(udpPort));
		} catch(IOException e) {
			running.set(false);
			fireSessionException(null, e);
		}
	}
	
	public void close() {
		if(running.getAndSet(false)) {
			handleClose();
		}
	}
	
	public boolean isBound() {
		return running.get();
	}
	
}
