package nl.u2.netlib.nio;

import java.util.concurrent.atomic.AtomicBoolean;

import nl.u2.netlib.AbstractConnection;
import nl.u2.netlib.EndPoint;
import nl.u2.netlib.listener.event.ConnectionDestroyedEvent;

public final class NioConnection extends AbstractConnection {

	private final AtomicBoolean closed = new AtomicBoolean();
	
	boolean udpRegistered;
	
	private NioTcpPipeline tcp;
	private NioUdpPipeline udp;
	
	NioConnection(EndPoint endPoint, int bufferSize) {
		super(endPoint);
		
		tcp = new NioTcpPipeline(this, bufferSize);
		udp = new NioUdpPipeline(this);
	}

	public NioTcpPipeline tcp() {
		return tcp;
	}

	public NioUdpPipeline udp() {
		return udp;
	}

	public void close() {
		if(!closed.getAndSet(true)) {
			tcp.close();
			
			endPoint().fireConnectionDestroyed(new ConnectionDestroyedEvent(this));
		}
	}

	public boolean isActive() {
		return tcp.isActive() && udp.isActive();
	}

}
