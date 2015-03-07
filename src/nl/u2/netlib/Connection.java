package nl.u2.netlib;

public interface Connection {

	Pipeline tcp();
	
	Pipeline udp();
	
	EndPoint endPoint();
	
	void close();
	
	boolean isActive();
	
}
