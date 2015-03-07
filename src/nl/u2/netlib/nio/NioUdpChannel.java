package nl.u2.netlib.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

import nl.u2.netlib.packet.Packet;
import nl.u2.netlib.util.DataUtil;

final class NioUdpChannel implements NioServerChannel {

	private final Object lock = new Object();
	
	private ByteBuffer writeBuffer;
	private ByteBuffer readBuffer;
	
	private DatagramChannel channel;
	private SelectionKey key;
	
	InetSocketAddress local;
	
	NioUdpChannel(int bufferSize) {
		writeBuffer = ByteBuffer.allocateDirect(bufferSize);
		readBuffer = ByteBuffer.allocate(bufferSize);
	}
	
	public void bind(Selector selector, SocketAddress address) throws Exception {
		clearBuffers();
		
		selector.wakeup();
		channel = selector.provider().openDatagramChannel();
		channel.bind(address);
		channel.configureBlocking(false);
		key = channel.register(selector, SelectionKey.OP_READ);
		
		local = (InetSocketAddress) channel.getLocalAddress();
	}
	
	void connect (Selector selector, InetSocketAddress remoteAddress) throws Exception {
		clearBuffers();
	
		channel = selector.provider().openDatagramChannel();
		channel.socket().connect(remoteAddress);
		channel.configureBlocking(false);
		key = channel.register(selector, SelectionKey.OP_READ);
		
		local = (InetSocketAddress) channel.getLocalAddress();
	}
	
	void write(Packet packet) throws Exception {
		DatagramChannel server = this.channel;
		if(server == null) {
			throw new ClosedChannelException();
		}
		
		byte[] data = packet.data();
		data = DataUtil.writeIntToArray(data, packet.opcode());
		//TODO encrypt data
		
		data = DataUtil.writeIntToArray(data, data.length);
		
		synchronized(lock) {
			try {
				writeBuffer.put(data, 0, data.length);
				writeBuffer.flip();
				
				server.send(writeBuffer, packet.address());
			} finally {
				writeBuffer.clear();
			}
		}
	}
	
	SocketAddress readAddressAndBuffer() throws Exception {
		DatagramChannel server = this.channel;
		if(server == null) {
			throw new ClosedChannelException();
		}
		
		return server.receive(readBuffer);
	}
	
	Packet readPacket(SocketAddress address) throws Exception {
		readBuffer.flip();
		try {
			if(readBuffer.remaining() < 4) {
				return null;
			}

			int length = readBuffer.getInt();
			if(length < 0) {
				throw new IOException("Invalid buffer length: " + length);
			}

			if(length > readBuffer.capacity()) {
				throw new IOException("Unable to read buffer larger than read buffer: " + length);
			}

			byte[] data = new byte[length];
			readBuffer.get(data, 0, length);

			if(readBuffer.hasRemaining()) {
				throw new IOException("Incorrect number of bytes ("+ readBuffer.remaining() + " remaining) used to receive buffer.");
			}
			
			//TODO decrypt data
			
			int opcode = DataUtil.readInt(data);
			length = data.length - 4;
			
			byte[] payload = new byte[length];
			System.arraycopy(data, 4, payload, 0, length);
			
			Packet packet = Packet.toPacket(payload, opcode, address);
			return packet;
		} finally {
			readBuffer.clear();
		}
	}
	
	private void clearBuffers() {
		writeBuffer.clear();
		readBuffer.clear();
	}

	public void close() throws Exception {
		if(channel != null) {
			try {
				channel.close();
			} catch(Exception e) {
				throw e;
			} finally {
				channel = null;
				
				if(key != null) {
					key.selector().wakeup();
				}
				
				clearBuffers();
			}
		}
	}

	public boolean isActive() {
		return channel != null && channel.isRegistered() && channel.isOpen();
	}

}
