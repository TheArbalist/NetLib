package nl.u2.netlib.oio;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import nl.u2.netlib.AbstractPipeline;
import nl.u2.netlib.Connection;
import nl.u2.netlib.EndPoint;
import nl.u2.netlib.TransmissionProtocol;
import nl.u2.netlib.listener.event.PacketReceivedEvent;
import nl.u2.netlib.packet.Packet;
import nl.u2.netlib.util.DataUtil;

public final class OioUdpPipeline extends AbstractPipeline implements OioClientChannel, Runnable{

	private final Object lock = new Object();
	
	private DatagramSocket socket;
	
	OioUdpPipeline(Connection connection) {
		super(connection);
	}
	
	public void connect(SocketAddress address) throws Exception {
		socket = new DatagramSocket();
		socket.connect(address);
	}
	
	public void run() {
		Connection connection = connection();
		EndPoint endPoint = connection.endPoint();
		
		try {
			while(!Thread.interrupted() && isActive()) {
				byte[] data = new byte[65536];
				DatagramPacket datagram = new DatagramPacket(data, data.length);
				socket.receive(datagram);
				data = datagram.getData();
				
				int length = DataUtil.readInt(data);
				byte[] payload = new byte[length];
				System.arraycopy(data, 4, payload, 0, length);
				
				//TODO decrypt payload
				
				int opcode = DataUtil.readInt(payload);
				length = payload.length - 4;
				
				data = new byte[length];
				System.arraycopy(payload, 4, data, 0, length);
				
				Packet packet = Packet.toPacket(data, opcode, socket.getRemoteSocketAddress());
				endPoint.firePacketReceived(new PacketReceivedEvent(connection, packet));
			}
		} catch(Throwable t) {
		} finally {
			connection.close();
		}
	}
	
	public void write(Packet packet) {
		byte[] data = packet.data();
		data = DataUtil.writeIntToArray(data, packet.opcode());
		//TODO encrypt data
		
		data = DataUtil.writeIntToArray(data, data.length);
		
		try {					
			synchronized(lock) {
				DatagramPacket datagram = new DatagramPacket(data, data.length, socket.getRemoteSocketAddress());
				socket.send(datagram);
			}
		} catch (Throwable t) {
		}
	}

	public void close() throws Exception {
		if(socket != null) {
			socket.close();
		}
		socket = null;
	}

	public InetSocketAddress localAddress() {
		if(socket == null) {
			return null;
		}
		
		return (InetSocketAddress) socket.getLocalSocketAddress();
	}

	public InetSocketAddress remoteAddress() {
		if(socket == null) {
			return null;
		}
		
		return (InetSocketAddress) socket.getRemoteSocketAddress();
	}

	public boolean isActive() {
		return socket != null && !socket.isClosed() && socket.isConnected();
	}

	public TransmissionProtocol protocol() {
		return TransmissionProtocol.UDP;
	}
	
}
