package nl.u2.netlib;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public interface Pipeline {

	public void write(ByteBuffer buffer) throws IOException;
	
	public InetSocketAddress remoteAddress();
	
	public InetSocketAddress localAddress();
	
	public TransmissionProtocol protocol();
	
}
