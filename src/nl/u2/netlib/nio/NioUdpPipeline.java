package nl.u2.netlib.nio;

import java.net.InetSocketAddress;

import nl.u2.netlib.AbstractPipeline;
import nl.u2.netlib.Connection;
import nl.u2.netlib.TransmissionProtocol;
import nl.u2.netlib.packet.Packet;

public final class NioUdpPipeline extends AbstractPipeline {

	InetSocketAddress local;
	InetSocketAddress remote;
	
	NioUdpChannel channel;
	
	NioUdpPipeline(Connection connection) {
		super(connection);
	}

	public void write(Packet packet) {
		try {
			channel.write(packet.address(remote));
		} catch(Throwable t) {
			connection().endPoint().fireExceptionThrown(t);
		}
	}

	public InetSocketAddress localAddress() {
		return local;
	}
	
	void remoteAddress(InetSocketAddress remote) {
		this.remote = remote;
	}

	public InetSocketAddress remoteAddress() {
		return remote;
	}
	
	public TransmissionProtocol protocol() {
		return TransmissionProtocol.UDP;
	}

	public boolean isActive() {
		return channel != null && channel.isActive();
	}

}
