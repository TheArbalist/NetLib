package nl.u2.netlib.nio;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import nl.u2.netlib.AbstractClient;

public class NioClient extends AbstractClient implements Runnable {

	private static final int DEFAULT_BUFFER_SIZE = 2048;
	
	private final Object updateLock = new Object();
	private ScheduledExecutorService executor;
	private int emptySelects = 0;
	private boolean connected;
	
	protected Selector selector;
	protected NioSession session;
	
	protected int bufferSize;
	
	public NioClient() {
		this(DEFAULT_BUFFER_SIZE);
	}
	
	public NioClient(int bufferSize) {
		try {
			selector = Selector.open();
		} catch(IOException e) {
			throw new RuntimeException("Error opening selector.", e);
		}
		
		this.bufferSize = bufferSize;
	}
	
	@Override
	protected void handleConnect(SocketAddress tcpAddress, SocketAddress udpAddress) throws IOException {
		synchronized(updateLock) {
			selector.wakeup();
			
			try {
				session = new NioSession(this, bufferSize);
				session.tcp.connect(session, selector, tcpAddress);
				
				executor = Executors.newSingleThreadScheduledExecutor();
				executor.scheduleAtFixedRate(this, 0, 25, TimeUnit.MILLISECONDS);
				
				connected = true;
				fireSessionConnected(session);
			} catch(IOException e) {
				close();
				throw e;
			}
		}
	}
	
	public final void run() {
		try {
			update();
		} catch (IOException e) {
			e.printStackTrace();
			
			close();
		}
	}
	
	protected void update() throws IOException {
		synchronized(updateLock) {
		}
		
		long start = System.currentTimeMillis();
		int select = selector.select(250);
		
		if(select != 0) {
			emptySelects = 0;
			Set<SelectionKey> keys = selector.selectedKeys();
			synchronized(keys) {
				for(Iterator<SelectionKey> i = keys.iterator(); i.hasNext();) {
					SelectionKey key = i.next();
					i.remove();
					
					NioSession session = (NioSession) key.attachment();
					try {
						if(session != null) {//TCP Read or write -> channel has been connected
							if(key.isReadable()) {
								readOperation();
							}
							
							if(key.isWritable()) {
								writeOperation();
							}
							
							continue;
						}
					} catch(CancelledKeyException ex) {
						if(session != null) {
							session.close();
						} else {
							key.channel().close();
						}
					}
				}
			}
		} else {
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
		}
	}
	
	private void readOperation() {
		try {
			while(true) {
				ByteBuffer buffer = session.tcp.readBuffer();
				if(buffer == null) {
					break;
				}
				
				fireSessionReceived(session, buffer);
			}
		} catch (IOException e) {
			session.close();
		}
	}
	
	private void writeOperation() {
		try {
			session.tcp.writeBuffer();
		} catch (IOException ex) {
			session.close();
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

		synchronized (updateLock) {
			selector.wakeup();
			try {
				selector.selectNow();
			} catch (IOException e) {
			}
		}
		
	}

}
