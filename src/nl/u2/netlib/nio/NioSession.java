package nl.u2.netlib.nio;

import java.util.concurrent.atomic.AtomicBoolean;

import nl.u2.netlib.AbstractSession;
import nl.u2.netlib.Client;
import nl.u2.netlib.EndPoint;

public class NioSession extends AbstractSession {

	private AtomicBoolean running = new AtomicBoolean(false);
	
	protected EndPoint endPoint;
	protected NioTcpPipeline tcp;
	protected NioUdpPipeline udp;
	
	protected NioSession(EndPoint endPoint, int bufferSize) {
		this.endPoint = endPoint;
		tcp = new NioTcpPipeline(bufferSize);
		udp = new NioUdpPipeline();
		
		running.set(true);
	}
	
	public NioTcpPipeline getTcpPipeline() {
		return tcp;
	}

	public NioPipeline getUdpPipeline() {
		return udp;
	}

	public void close() {
		if(running.getAndSet(false)) {
			tcp.close();
			
			if(endPoint instanceof Client) {
				((Client) endPoint).close();
			} else if (endPoint instanceof NioServer){
				((NioServer) endPoint).closeOperation(this);
			}
		}
	}
	
	public EndPoint getEndPoint() {
		return endPoint;
	}
	
	public boolean isActive() {
		return running.get();
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("Session[Type=NIO, State=");
		if(running.get()) {
			builder.append("active, TCP=").append(tcp.remoteAddress()).
			append(", UDP=").append(udp.remoteAddress());
		} else {
			builder.append("inactive");
		}
		return builder.append("]").toString();
	}
	
}
