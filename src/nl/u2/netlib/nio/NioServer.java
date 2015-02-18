package nl.u2.netlib.nio;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import nl.u2.netlib.AbstractServer;

public class NioServer extends AbstractServer implements Runnable {

	private static final int DEFAULT_BUFFER_SIZE = 2048;
	
	private final Object updateLock = new Object();
	private ScheduledExecutorService executor;
	private int emptySelects = 0;
	
	protected Selector selector;
	protected ServerSocketChannel server;
	
	protected int bufferSize;
	
	public NioServer() {
		this(DEFAULT_BUFFER_SIZE);
	}
	
	public NioServer(int bufferSize) {
		this.bufferSize = bufferSize;
		
		try {
			selector = Selector.open();
		} catch(IOException e) {
			throw new RuntimeException("Error opening selector.", e);
		}
	}
	
	@Override
	protected void handleBind(SocketAddress tcpAddress, SocketAddress udpAddress) throws IOException {
		synchronized(updateLock) {
			selector.wakeup();
			try {
				server = selector.provider().openServerSocketChannel();
				server.bind(tcpAddress);
				server.configureBlocking(false);
				server.register(selector, SelectionKey.OP_ACCEPT);
				
				executor = Executors.newSingleThreadScheduledExecutor();
				executor.scheduleAtFixedRate(this, 0, 25, TimeUnit.MILLISECONDS);
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
						if(session != null) {//TCP Read or write -> channel has been accepted
							if(key.isReadable()) {
								read(session);
							}
							if(key.isWritable()) {
								write(session);
							}
							
							continue;
						}
						
						if(key.isAcceptable()) {
							accept();
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
	
	private void read(NioSession session) {
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
	
	private void write(NioSession session) {
		try {
			session.tcp.writeBuffer();
		} catch (IOException ex) {
			session.close();
		}
	}
	
	private void accept() {
		ServerSocketChannel server = this.server;
		if(server == null) {
			return;
		}
		
		try {
			SocketChannel channel = server.accept();
			if(channel != null) {
				NioSession session = new NioSession(this, bufferSize);
				SelectionKey key = session.tcp.accept(selector, channel);
				key.attach(session);
				
				fireSessionConnected(session);
			}
		} catch(IOException e) {
		}
	}

	@Override
	protected void handleClose() {
		if(executor != null) {
			executor.shutdownNow();
			executor = null;
		}
		
		if(server != null) {
			try {
				server.close();
			} catch (IOException e) {
			}
			server = null;
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
