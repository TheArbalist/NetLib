package nl.u2.netlib.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import nl.u2.netlib.AbstractServer;
import nl.u2.netlib.TransmissionProtocol;

public class NioServer extends AbstractServer implements Runnable {

	private static final int DEFAULT_BUFFER_SIZE = 2048;
	
	private final Map<SocketAddress, NioSession> sessions = new ConcurrentHashMap<SocketAddress, NioSession>();
	private final Object updateLock = new Object();
	private ExecutorService executor;
	private int emptySelects = 0;
	
	protected Selector selector;
	protected ServerSocketChannel server;
	protected NioDatagramPipeline datagramPipeline;
	
	protected int bufferSize;
	
	public NioServer() {
		this(DEFAULT_BUFFER_SIZE);
	}
	
	public NioServer(int bufferSize) {
		try {
			selector = Selector.open();
		} catch(IOException e) {
			throw new RuntimeException("Error opening selector.", e);
		}
		
		datagramPipeline = new NioDatagramPipeline(this.bufferSize = bufferSize);
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
				
				datagramPipeline.bind(selector, udpAddress);
				
				executor = Executors.newSingleThreadExecutor();
				executor.execute(this);
			} catch(IOException e) {
				close();
				throw e;
			}
		}
	}
	
	public final void run() {
		try {
			for(;;) {
				update();
			}
		} catch (IOException e) {
			close();
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
					
					NioSession session = (NioSession) key.attachment();
					try {
						if(session != null) {
							if(key.isReadable()) {
								readOperation(session);
							}
							
							if(key.isWritable()) {
								writeOperation(session);
							}
							
							continue;
						}
						
						if(key.isAcceptable()) {
							acceptOperation();
							continue;
						}
						
						InetSocketAddress fromAddress;
						try {
							fromAddress = datagramPipeline.readAddressAndBuffer();
						} catch(IOException e) {
							e.printStackTrace();
							continue;
						}
						
						if(fromAddress == null) {
							continue;
						}
						
						session = sessions.get(fromAddress);
						ByteBuffer buffer;
						try {
							buffer = datagramPipeline.readBuffer();
						} catch(IOException e) {
							e.printStackTrace();
							continue;
						}
						
						if(session != null) {
							fireSessionReceived(session, TransmissionProtocol.UDP, buffer);
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
		}
	}
	
	private void readOperation(NioSession session) {
		try {
			while(true) {
				ByteBuffer buffer = session.tcp.readBuffer();
				if(buffer == null) {
					break;
				}
				
				if(!sessions.containsValue(session)) {
					if(buffer.remaining() >= 4) {
						String ip = session.tcp.remoteAddress().getAddress().getHostAddress();
						int port = buffer.getInt();
						
						InetSocketAddress address = new InetSocketAddress(ip, port);
						session.udp.remote = address;
						
						sessions.put(address, session);
						fireSessionConnected(session);
						continue;
					}
				}
				
				fireSessionReceived(session, TransmissionProtocol.TCP, buffer);
			}
		} catch (IOException e) {
			session.close();
		}
	}
	
	private void writeOperation(NioSession session) {
		try {
			session.tcp.writeBuffer();
		} catch (IOException ex) {
			session.close();
		}
	}
	
	private void acceptOperation() {
		ServerSocketChannel server = this.server;
		if(server == null) {
			return;
		}
		
		try {
			SocketChannel channel = server.accept();
			if(channel != null) {
				NioSession session = new NioSession(this, bufferSize);
				session.udp.pipeline = datagramPipeline;
				session.udp.local = (InetSocketAddress) datagramPipeline.channel.getLocalAddress();
				
				SelectionKey key = session.tcp.accept(selector, channel);
				key.attach(session);
			}
		} catch(IOException e) {
		}
	}
	
	protected void closeOperation(NioSession session) {
		sessions.remove(session.udp.remote);
		fireSessionDisconnected(session);	
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
		StringBuilder builder = new StringBuilder("Server[Type=NIO, State=");
		if(isBound()) {
			builder.append("bound, TCP=").append(server.socket().getLocalPort()).
			append(", UDP=").append(datagramPipeline.channel.socket().getLocalPort());
		} else {
			builder.append("idle");
		}
		return builder.append("]").toString();
	}

}
