package nl.u2.netlib.oio;

import java.io.EOFException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import nl.u2.netlib.AbstractPipeline;
import nl.u2.netlib.Connection;
import nl.u2.netlib.EndPoint;
import nl.u2.netlib.TransmissionProtocol;
import nl.u2.netlib.listener.event.PacketReceivedEvent;
import nl.u2.netlib.packet.Packet;
import nl.u2.netlib.util.DataUtil;

public final class OioTcpPipeline extends AbstractPipeline implements OioClientChannel, Runnable {
	
	private Socket socket;
	private InputStream is;
	private OutputStream os;
	
	protected OioTcpPipeline(Connection connection) {
		super(connection);
	}
	
	public void connect(SocketAddress address) throws Exception {
		socket = new Socket();
		socket.connect(address);
		
		is = socket.getInputStream();
		os = socket.getOutputStream();
	}

	public void run() {
		Connection connection = connection();
		EndPoint endPoint = connection.endPoint();
		
		try {
			while(!Thread.interrupted() && isActive()) {
				byte[] data = new byte[4];
				int n = 0;
				while(n < 4) {
					int count = is.read(data, n, 4 - n);
					if (count < 0) {
						throw new EOFException();
					}
					n += count;
				}
				
				int length = DataUtil.readInt(data);
				
				data = new byte[length];
				is.read(data, 0, length);
				
				//TODO decrypt data
				
				int opcode = DataUtil.readInt(data);
				length = data.length - 4;
				
				byte[] payload = new byte[length];
				System.arraycopy(data, 4, payload, 0, length);
				
				Packet packet = Packet.toPacket(payload, opcode, null);
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
			synchronized(os) {
				os.write(data, 0, data.length);
			}
		} catch(Throwable t) {
			connection().endPoint().fireExceptionThrown(t);
		}
	}
	
	public void close() throws Exception {
		try {
			socket.close();
		} catch(Throwable t) {
		}
		socket = null;
		
		try {
			is.close();
		} catch(Throwable t) {
		}
		is = null;
		
		try {
			os.close();
		} catch(Throwable t) {
		}
		os = null;
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

	public TransmissionProtocol protocol() {
		return TransmissionProtocol.TCP;
	}
	
	public boolean isActive() {
		return socket != null && !socket.isClosed() && socket.isConnected();
	}


}
