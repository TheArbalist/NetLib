package nl.u2.netlib;

import java.io.IOException;

public interface Server extends EndPoint {

	public void bind(int tcpPort, int udpPort) throws IOException;
	
	public void close();
	
}
