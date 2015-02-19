package nl.u2.netlib.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import nl.u2.netlib.TransmissionProtocol;
import nl.u2.netlib.exception.InvalidReadException;

public class NioTcpPipeline extends NioPipeline {

	private final Object writeLock = new Object();
	private int currentBufferLength = -1;
	
	protected SocketChannel channel;
	protected SelectionKey key;
	protected ByteBuffer writeBuffer;
	protected ByteBuffer readBuffer;
	
	protected NioTcpPipeline(int bufferSize) {
		writeBuffer = ByteBuffer.allocate(bufferSize);
		readBuffer = ByteBuffer.allocate(bufferSize);
		readBuffer.flip();
	}
	
	protected void connect(NioSession session, Selector selector, SocketAddress adress) throws IOException {
		writeBuffer.clear();
		readBuffer.clear();
		readBuffer.flip();
		currentBufferLength = -1;
		
		channel = selector.provider().openSocketChannel();
		channel.socket().setTcpNoDelay(true);
		channel.connect(adress);
		channel.configureBlocking(false);

		key = channel.register(selector, SelectionKey.OP_READ);
		key.attach(session);
	}
	
	protected SelectionKey accept(Selector selector, SocketChannel channel) throws IOException {
		writeBuffer.clear();
		readBuffer.clear();
		readBuffer.flip();
		currentBufferLength = -1;
		
		this.channel = channel;
		channel.configureBlocking(false);
		channel.socket().setTcpNoDelay(true);

		return key = channel.register(selector, SelectionKey.OP_READ);
	}
	
	//TODO remove copy if possible
	public void write(ByteBuffer buffer) throws IOException {
		SocketChannel channel = this.channel;
		if(channel == null) {
			throw new ClosedChannelException();
		}
		
		synchronized(writeLock) {
			int start = writeBuffer.position();
			if(start < 0 || start + 4 > writeBuffer.limit()) {
				writeBuffer.rewind();
				return;
			}
			
			writeBuffer.position(start + 4);
			writeBuffer.put(buffer);
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
	}
		
	private boolean writeToSocket() throws IOException {
		SocketChannel channel = this.channel;
		if(channel == null) {
			throw new ClosedChannelException();
		}
		
		ByteBuffer buffer = writeBuffer;
		buffer.flip();
		while (buffer.hasRemaining()) {
			if (channel.write(buffer) == 0)
				break;
		}
		buffer.compact();

		return buffer.position() == 0;
	}
	
	@Override
	protected ByteBuffer readBuffer() throws IOException {
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
				throw new InvalidReadException("Invalid buffer length: " + currentBufferLength);
			}
			
			if(currentBufferLength > readBuffer.capacity()) {
				throw new InvalidReadException("Unable to read buffer larger than read buffer: " + currentBufferLength);
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
			throw new InvalidReadException("Incorrect number of bytes ("+ (start + length - readBuffer.position())
									+ " remaining) used to deserialize object: " + buffer);
		}
		
		return buffer;
	}
	
	@Override
	protected void writeBuffer() throws IOException {
		synchronized (writeLock) {
			if (writeToSocket()) {
				key.interestOps(SelectionKey.OP_READ);
			}
		}
	}
	
	@Override
	protected void close() {
		try {
			if(channel != null) {
				channel.close();
				channel = null;
				if(key != null) {
					key.selector().wakeup();
				}
			}
		} catch(IOException e) {
		}
	}

	public InetSocketAddress remoteAddress() {
		SocketChannel channel = this.channel;
		if(channel == null) {
			return null;
		}
		
		return (InetSocketAddress) channel.socket().getRemoteSocketAddress();
	}

	public InetSocketAddress localAddress() {
		SocketChannel channel = this.channel;
		if(channel == null) {
			return null;
		}
		
		return (InetSocketAddress) channel.socket().getLocalSocketAddress();
	}

	public TransmissionProtocol protocol() {
		return TransmissionProtocol.TCP;
	}

}
