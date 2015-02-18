package nl.u2.netlib;

import java.io.IOException;

public interface Client extends EndPoint {

	public void connect(String host, int tcpPort, int udpPort) throws IOException;
	
	public void close();
	
	public boolean isConnected();
	
}
