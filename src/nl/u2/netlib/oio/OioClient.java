package nl.u2.netlib.oio;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import nl.u2.netlib.AbstractEndPoint;
import nl.u2.netlib.Client;
import nl.u2.netlib.EndPoint;
import nl.u2.netlib.Pipeline;
import nl.u2.netlib.listener.event.ConnectionDestroyedEvent;
import nl.u2.netlib.listener.event.ConnectionEstablishedEvent;
import nl.u2.netlib.packet.PacketBuilder;
import nl.u2.netlib.packet.PacketHeaders;

public final class OioClient extends AbstractEndPoint implements Client {

	private final Object lock = new Object();
	private final AtomicBoolean closed = new AtomicBoolean(true);
	
	private ExecutorService executor;
	
	private OioTcpPipeline tcp;
	private OioUdpPipeline udp;
	
	public OioClient() {
		tcp = new OioTcpPipeline(this);
		udp = new OioUdpPipeline(this);
	}
	
	public void connect(String host, int tcpPort, int udpPort) throws Exception {
		close();
		
		synchronized(lock) {
			executor = Executors.newFixedThreadPool(2);
			
			try {
				tcp.connect(new InetSocketAddress(host, tcpPort));
				udp.connect(new InetSocketAddress(host, udpPort));
				
				executor.execute(tcp);
				executor.execute(udp);
				
				closed.set(false);
				
				tcp.write(new PacketBuilder().opcode(PacketHeaders.PACKET_UDP_REGISTRATION).writeInt(udp.localAddress().getPort()).toPacket());
				super.fireConnectionEstablished(new ConnectionEstablishedEvent(this));
			} catch(Exception e) {
				close();
				
				throw e;
			}
		}
	}
	
	public void close() {
		if(!closed.getAndSet(true)) {
			if(executor != null) {
				executor.shutdownNow();
				executor = null;
			}
			
			try {
				tcp.close();
			} catch(Throwable t) {
				super.fireExceptionThrown(t);
			}
			
			try {
				udp.close();
			} catch(Throwable t) {
				super.fireExceptionThrown(t);
			}
			
			super.fireConnectionDestroyed(new ConnectionDestroyedEvent(this));
		}
	}

	public Pipeline tcp() {
		return tcp;
	}

	public Pipeline udp() {
		return udp;
	}

	public EndPoint endPoint() {
		return this;
	}
	
	public boolean isActive() {
		return tcp.isActive() && udp.isActive();
	}

}
