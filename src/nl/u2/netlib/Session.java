package nl.u2.netlib;

public interface Session {
	
	public Pipeline getTcpPipeline();
	
	public Pipeline getUdpPipeline();
	
	public void close();
	
}
