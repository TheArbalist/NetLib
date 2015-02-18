package nl.u2.netlib.nio;

import nl.u2.netlib.EndPoint;
import nl.u2.netlib.Session;

public class NioSession implements Session {

	private boolean closed;
	
	protected EndPoint endPoint;
	protected NioTcpPipeline tcp;
	
	protected NioSession(EndPoint endPoint, int bufferSize) {
		this.endPoint = endPoint;
		tcp = new NioTcpPipeline(bufferSize);
	}
	
	public NioTcpPipeline getTcpPipeline() {
		return tcp;
	}

	public NioPipeline getUdpPipeline() {
		// TODO Auto-generated method stub
		return null;
	}

	public void close() {
		if(!closed) {
			tcp.close();
			closed = true;
			
			endPoint.fireSessionDisconnected(this);
		}
	}

	
	
}
