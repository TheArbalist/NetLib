package nl.u2.netlib.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import nl.u2.netlib.AbstractClient;
import nl.u2.netlib.TransmissionProtocol;

public class NioClient extends AbstractClient implements Runnable {

	private static final int DEFAULT_BUFFER_SIZE = 2048;
	
	private final Object updateLock = new Object();
	private ExecutorService executor;
	private int emptySelects = 0;
	private boolean connected;
	
	protected Selector selector;
	protected NioSession session;
	protected NioDatagramPipeline datagramPipeline;
	
	protected int bufferSize;
	
	public NioClient() {
		this(DEFAULT_BUFFER_SIZE);
	}
	
	public NioClient(int bufferSize) {
		try {
			selector = Selector.open();
		} catch(IOException e) {
			fireSessionException(session, e);
		}
		
		datagramPipeline = new NioDatagramPipeline(this.bufferSize = bufferSize);
	}
	
	@Override
	protected void handleConnect(SocketAddress tcpAddress, SocketAddress udpAddress) throws IOException {
		synchronized(updateLock) {
			selector.wakeup();
			
			try {
				session = new NioSession(this, bufferSize);
				session.tcp.connect(session, selector, tcpAddress);
				
				datagramPipeline.connect(selector, udpAddress);
				session.udp.pipeline = datagramPipeline;
				
				InetSocketAddress address = (InetSocketAddress) datagramPipeline.channel.getLocalAddress();
				session.udp.local = address;
				session.udp.remote = (InetSocketAddress) udpAddress;
				
				executor = Executors.newSingleThreadExecutor();
				executor.execute(this);
				
				connected = true;
				handshake(address);
				fireSessionConnected(session);
			} catch(IOException e) {
				close();
				throw e;
			}
		}
	}
	
	private void handshake(InetSocketAddress address) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(4);
		buffer.putInt(address.getPort());
		buffer.flip();
		
		session.tcp.write(buffer);
	}
	
	public final void run() {
		try {
			for(;;) {
				update();
			}
		} catch(IOException e) {
			close();
			fireSessionException(session, e);
		}
	}
	
	protected void update() throws IOException {
		synchronized(updateLock) {
		}
		
		long start = System.currentTimeMillis();
		int select = selector.select(250);
		
		if(select == 0) {
			emptySelects++;
			if(emptySelects == 100) {
				emptySelects = 0;
				long elapsed = System.currentTimeMillis() - start;
				try {
					if(elapsed < 25) {
						Thread.sleep(25 - elapsed);
					}
				} catch(InterruptedException e) {
				}
			}
		} else {
			emptySelects = 0;
			Set<SelectionKey> keys = selector.selectedKeys();
			synchronized(keys) {
				for(Iterator<SelectionKey> i = keys.iterator(); i.hasNext();) {
					SelectionKey key = i.next();
					i.remove();
					
					try {
						if(key.isReadable()) {
							if(key.attachment() != null) {
								while(true) {
									ByteBuffer buffer = session.tcp.readBuffer();
									if(buffer == null) {
										break;
									}
									
									fireSessionReceived(session, TransmissionProtocol.TCP, buffer);
								}
							} else {
								if(datagramPipeline.readAddressAndBuffer() == null) {
									continue;
								}
								
								ByteBuffer buffer;
								try {
									buffer = datagramPipeline.readBuffer();
								} catch(IOException e) {
									fireSessionException(this.session, e);
									continue;
								}
								
								fireSessionReceived(this.session, TransmissionProtocol.UDP, buffer);
							}
						}
						
						if(key.isWritable()) {
							session.tcp.writeBuffer();
						}
					} catch(CancelledKeyException e) {
						fireSessionException(session, e);
					}
				}
			}
		}
	}
	
	@Override
	protected void handleClose() {
		if(executor != null) {
			executor.shutdownNow();
			executor = null;
		}
		
		if(session != null) {
			session.close();
			if(connected) {
				connected = false;
				fireSessionDisconnected(session);
			}
			session = null;
		}
		
		datagramPipeline.close();

		synchronized (updateLock) {
			selector.wakeup();
			try {
				selector.selectNow();
			} catch (IOException e) {
			}
		}
		
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("Client[Type=NIO, State=");
		if(isConnected()) {
			builder.append("connected, TCP=").append(session.tcp).
					append(", UDP=").append(session.udp);
		} else {
			builder.append("inactive");
		}
		return builder.append("]").toString();
	}

}
