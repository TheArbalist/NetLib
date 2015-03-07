package nl.u2.netlib;

import java.net.InetSocketAddress;

import nl.u2.netlib.packet.Packet;

public interface Pipeline {
	
	void write(Packet packet);
	
	Connection connection();

	InetSocketAddress localAddress();
	
	InetSocketAddress remoteAddress();
	
	TransmissionProtocol protocol();
	
	boolean isActive();
	
}
