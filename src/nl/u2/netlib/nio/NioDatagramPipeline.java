package nl.u2.netlib.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

public class NioDatagramPipeline {
	
	private final Object writeLock = new Object();
	
	protected DatagramChannel channel;
	protected SelectionKey key;
	protected ByteBuffer writeBuffer;
	protected ByteBuffer readBuffer;
	
	protected NioDatagramPipeline(int bufferSize) {
		writeBuffer = ByteBuffer.allocateDirect(bufferSize);
		readBuffer = ByteBuffer.allocate(bufferSize);
		readBuffer.flip();
	}	
	
	protected void bind(Selector selector, SocketAddress udpAddress) throws IOException {
		readBuffer.clear();
		writeBuffer.clear();
		try {
			channel = selector.provider().openDatagramChannel();
			channel.bind(udpAddress);
			channel.configureBlocking(false);
			key = channel.register(selector, SelectionKey.OP_READ);

		} catch(IOException e) {
			close();
			throw e;
		}
	}
	
	protected void connect(Selector selector, SocketAddress address) throws IOException {
		readBuffer.clear();
		writeBuffer.clear();
		try {
			channel = selector.provider().openDatagramChannel();
			channel.bind(null);
			channel.connect(address);
			channel.configureBlocking(false);

			key = channel.register(selector, SelectionKey.OP_READ);
		} catch(IOException e) {
			close();
			throw e;
		}
	}
	
	protected int write(InetSocketAddress address, ByteBuffer buffer) throws IOException {
		DatagramChannel channel = this.channel;
		if(channel == null) {
			throw new ClosedChannelException();
		}
		
		synchronized(writeLock) {
			try {
				writeBuffer.put(buffer);
				writeBuffer.flip();
				int length = writeBuffer.limit();
				channel.send(writeBuffer, address);

				boolean wasFullWrite = !writeBuffer.hasRemaining();
				return wasFullWrite ? length : -1;
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
			int length = readBuffer.remaining();
			byte[] data = new byte[length];
			readBuffer.get(data, 0, length);
			
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
