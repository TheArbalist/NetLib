package nl.u2.netlib.nio;

import java.util.concurrent.atomic.AtomicBoolean;

import nl.u2.netlib.Client;
import nl.u2.netlib.EndPoint;
import nl.u2.netlib.Session;

public class NioSession implements Session {

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
			} else {
				endPoint.fireSessionDisconnected(this);	
			}
		}
	}
	
}
