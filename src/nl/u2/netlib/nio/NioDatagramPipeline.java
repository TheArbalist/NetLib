package nl.u2.netlib.nio;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

import nl.u2.netlib.exception.InvalidReadException;

public class NioDatagramPipeline {
	
	private final Object writeLock = new Object();
	
	protected DatagramChannel channel;
	protected SelectionKey key;
	protected ByteBuffer writeBuffer;
	protected ByteBuffer readBuffer;
	
	protected NioDatagramPipeline(int bufferSize) {
		writeBuffer = ByteBuffer.allocateDirect(bufferSize);
		readBuffer = ByteBuffer.allocate(bufferSize);
	}	
	
	protected void bind(Selector selector, SocketAddress address) throws IOException {
		writeBuffer.clear();
		readBuffer.clear();
		try {
			channel = selector.provider().openDatagramChannel();
			channel.configureBlocking(false);
			
			DatagramSocket socket = channel.socket();
			socket.setReuseAddress(true);
			socket.bind(address);
			
			key = channel.register(selector, SelectionKey.OP_READ);
		} catch(IOException e) {
			close();
			throw e;
		}
	}
	
	protected void connect(Selector selector, SocketAddress address) throws IOException {
		writeBuffer.clear();
		readBuffer.clear();
		try {
			channel = selector.provider().openDatagramChannel();
			channel.configureBlocking(false);
			channel.connect(address);
			
			key = channel.register(selector, SelectionKey.OP_READ);
		} catch(IOException e) {
			close();
			throw e;
		}
	}
	
	protected void write(InetSocketAddress address, ByteBuffer buffer) throws IOException {
		DatagramChannel channel = this.channel;
		if(channel == null) {
			throw new ClosedChannelException();
		}
		
		synchronized(writeLock) {
			try {
				writeBuffer.putInt(buffer.remaining());
				writeBuffer.put(buffer);
				writeBuffer.flip();
				
				channel.send(writeBuffer, address);
			} finally {
				writeBuffer.clear();
			}
		}
	}
	
	protected InetSocketAddress readAddressAndBuffer() throws IOException {
		DatagramChannel channel = this.channel;
		if(channel == null) {
			throw new ClosedChannelException();
		}
		
		return (InetSocketAddress) channel.receive(readBuffer);
	}
	
	protected ByteBuffer readBuffer() throws IOException{
		readBuffer.flip();
		try {
			if(readBuffer.remaining() < 4) {
				return null;
			}
			
			int length = readBuffer.getInt();
			if(length < 0 || length > readBuffer.capacity()) {
				return null;
			}
			
			byte[] data = new byte[length];
			readBuffer.get(data, 0, length);
			
			if(readBuffer.hasRemaining()) {
				throw new InvalidReadException("Incorrect number of bytes (" + readBuffer.remaining()
											+ " remaining) used to receive buffer.");
			}
			
			return ByteBuffer.wrap(data, 0, length);
		} finally {
			readBuffer.clear();
		}
	}
	
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
}
