package nl.u2.netlib.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import nl.u2.netlib.AbstractPipeline;
import nl.u2.netlib.Connection;
import nl.u2.netlib.TransmissionProtocol;
import nl.u2.netlib.packet.Packet;
import nl.u2.netlib.util.DataUtil;

public final class NioTcpPipeline extends AbstractPipeline {

	private final Object lock = new Object();
	
	private ByteBuffer writeBuffer;
	private ByteBuffer readBuffer;
	
	private SocketChannel channel;
	private SelectionKey key;
	
	private int currentBufferLength = -1;
	
	NioTcpPipeline(Connection connection, int bufferSize) {
		super(connection);
		
		writeBuffer = ByteBuffer.allocate(bufferSize);
		readBuffer = ByteBuffer.allocate(bufferSize);
		readBuffer.flip();
	}
	
	SelectionKey accept(Selector selector, SocketChannel channel) throws Exception {
		clearBuffers();
		readBuffer.flip();
		currentBufferLength = -1;

		this.channel = channel;
		
		channel.configureBlocking(false);
		key = channel.register(selector, SelectionKey.OP_READ);

		return key;
	}
	
	void connect(Selector selector, SocketAddress address) throws Exception {
		clearBuffers();
		readBuffer.flip();
		currentBufferLength = -1;
		
		channel = selector.provider().openSocketChannel();
		Socket socket = channel.socket();
		socket.setTcpNoDelay(true);
		socket.connect(address);
		channel.configureBlocking(false);

		key = channel.register(selector, SelectionKey.OP_READ);
		key.attach(this);
	}
	
	public void write(Packet packet) {
		try {
			SocketChannel channel = this.channel;
			if(channel == null) {
				throw new ClosedChannelException();
			}
			
			byte[] data = packet.data();
			data = DataUtil.writeIntToArray(data, packet.opcode());
			//TODO encrypt data
			
			synchronized(lock) {
				int start = writeBuffer.position();
				if(start < 0 || start + 4 > writeBuffer.limit()) {
					writeBuffer.rewind();
					return;
				}
				
				writeBuffer.position(start + 4);
				writeBuffer.put(data);
				int end = writeBuffer.position();
				
				writeBuffer.position(start);
				writeBuffer.putInt(end - 4 - start);
				writeBuffer.position(end);
				
				if(start == 0 && !writeToSocket()) {
					key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
				} else {
					key.selector().wakeup();
				}
			}
		} catch(Throwable t) {
			connection().endPoint().fireExceptionThrown(t);
		}
	}
	
	void writeBuffer() throws Exception {
		synchronized(lock) {
			if(writeToSocket()) {
				key.interestOps(SelectionKey.OP_READ);
			}
		}
	}
	
	private boolean writeToSocket() throws Exception {
		SocketChannel channel = this.channel;
		if(channel == null) {
			throw new ClosedChannelException();
		}

		ByteBuffer buffer = writeBuffer;
		buffer.flip();

		while(buffer.hasRemaining()) {
			if(channel.write(buffer) == 0)
				break;
		}
		buffer.compact();

		return buffer.position() == 0;
	}
	
	Packet readPacket() throws Exception {
		SocketChannel channel = this.channel;
		if(channel == null) {
			throw new ClosedChannelException();
		}
		
		if(currentBufferLength == -1) {
			if(readBuffer.remaining() < 4) {
				readBuffer.compact();
				int read = channel.read(readBuffer);
				readBuffer.flip();
				if(read == -1) {
					throw new ClosedChannelException();
				}
				
				if(readBuffer.remaining() < 4) {
					return null;
				}
			}
			currentBufferLength = readBuffer.getInt();
			
			if(currentBufferLength < 0) {
				throw new IOException("Invalid buffer length: " + currentBufferLength);
			}
			
			if(currentBufferLength > readBuffer.capacity()) {
				throw new IOException("Unable to read buffer larger than read buffer: " + currentBufferLength);
			}
		}
		
		int length = currentBufferLength;
		if(readBuffer.remaining() < length) {
			readBuffer.compact();
			int read = channel.read(readBuffer);
			readBuffer.flip();
			
			if(read == -1) {
				throw new ClosedChannelException();
			}
			
			System.out.println(readBuffer.remaining() + "; " + length);
			
			if(readBuffer.remaining() < length) {
				return null;
			}
		}
		currentBufferLength = -1;
		
		int start = readBuffer.position();
		int limit = readBuffer.limit();
		readBuffer.limit(start + length);
		
		byte[] data = new byte[length];
		readBuffer.get(data, 0, length);
		
		ByteBuffer buffer = ByteBuffer.wrap(data, 0, length);
		readBuffer.limit(limit);
		
		if (readBuffer.position() - start != length) {
			throw new IOException("Incorrect number of bytes ("+ (start + length - readBuffer.position())
									+ " remaining) used to deserialize object: " + buffer);
		}
		
		//TODO decrypt data
		
		int opcode = DataUtil.readInt(data);
		length = data.length - 4;
		
		byte[] payload = new byte[length];
		System.arraycopy(data, 4, payload, 0, length);
		
		Packet packet = Packet.toPacket(payload, opcode, null);
		return packet;
	}
	
	public void close() {
		try {
			channel.close();
		} catch(Throwable t) {
		}
		channel = null;
		
		if(key != null) {
			key.selector().wakeup();
		}
		
		clearBuffers();
	}

	private void clearBuffers() {
		writeBuffer.clear();
		readBuffer.clear();
	}

	public InetSocketAddress localAddress() {
		SocketChannel channel = this.channel;
		if(channel == null) {
			return null;
		}
		
		return (InetSocketAddress) channel.socket().getLocalSocketAddress();
	}

	public InetSocketAddress remoteAddress() {
		SocketChannel channel = this.channel;
		if(channel == null) {
			return null;
		}
		
		return (InetSocketAddress) channel.socket().getRemoteSocketAddress();
	}

	public final TransmissionProtocol protocol() {
		return TransmissionProtocol.TCP;
	}

	public boolean isActive() {
		return channel != null && channel.isConnected();
	}
}
