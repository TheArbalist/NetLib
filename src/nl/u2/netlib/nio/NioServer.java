package nl.u2.netlib.nio;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import nl.u2.netlib.AbstractEndPoint;
import nl.u2.netlib.Server;
import nl.u2.netlib.exception.InvalidReadException;
import nl.u2.netlib.listener.event.ConnectionEstablishedEvent;
import nl.u2.netlib.listener.event.PacketReceivedEvent;
import nl.u2.netlib.packet.Packet;
import nl.u2.netlib.packet.PacketHeaders;

public final class NioServer extends AbstractEndPoint implements Server, Runnable {

	private static final int DEFAULT_BUFFER_SIZE = 1024;
	
	private final Map<SocketAddress, NioConnection> connections = new ConcurrentHashMap<SocketAddress, NioConnection>();
	private final Object lock = new Object();
	private final AtomicBoolean closed = new AtomicBoolean();

	private ExecutorService executor;
	private Selector selector;
	private int emptySelects;
	private int bufferSize;
	
	private NioTcpServer tcp;
	private NioUdpChannel udp;
	
	public NioServer() {
		this(DEFAULT_BUFFER_SIZE);
	}
	
	public NioServer(int bufferSize) {
		try {
			selector = Selector.open();
		} catch(Throwable t) {
			super.fireExceptionThrown(t);
		}
		
		this.bufferSize = bufferSize;
		
		tcp = new NioTcpServer();
		udp = new NioUdpChannel(bufferSize);
	}
	
	public void bind(int tcpPort, int udpPort) throws Exception {
		close();
		
		synchronized(lock) {
			try {
				tcp.bind(selector, new InetSocketAddress(tcpPort));
				udp.bind(selector, new InetSocketAddress(udpPort));
				
				executor = Executors.newSingleThreadExecutor();
				executor.execute(this);
				
				closed.set(false);
			} catch(Exception e) {
				close();
				
				throw e;
			}
		}
	}
	
	public void run() {
		try {
			while(!Thread.interrupted()) {
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
					for(Iterator<SelectionKey> i = keys.iterator(); i.hasNext();) {
						SelectionKey key = i.next();
						i.remove();
						
						NioConnection connection = (NioConnection) key.attachment();
						try {
							if(connection != null) {//TCP Read or write.
								if(key.isReadable()) {
									try {
										while(true) {
											Packet packet = connection.tcp().readPacket();
											if (packet == null) {
												break;
											}
											
											if(!connection.udpRegistered && packet.opcode() == PacketHeaders.PACKET_UDP_REGISTRATION) {
												String ip = connection.tcp().remoteAddress().getAddress().getHostAddress();
												int port = packet.readInt();
												InetSocketAddress address = new InetSocketAddress(ip, port);
												
												connection.udp().remote = address;
												connections.put(address, connection);
												
												connection.udpRegistered = true;
												super.fireConnectionEstablished(new ConnectionEstablishedEvent(connection));
												continue;
											}

											super.firePacketReceived(new PacketReceivedEvent(connection, packet));
										}
									} catch(Throwable t) {
										if(t.getClass() == InvalidReadException.class) {
											super.fireExceptionThrown(t);
										} else {
											connection.close();
										}
										
										continue;
									}
								}
								
								if(key.isWritable()) {
									try {
										connection.tcp().writeBuffer();
									} catch(Throwable t) {
										connection.close();
										continue;
									}
								}
								
								continue;
							}
							
							if(key.isAcceptable()) {//TCP Accept
								try {
									SocketChannel channel = tcp.server.accept();
									connection = new NioConnection(this, bufferSize);
									
									NioUdpPipeline pipeline = connection.udp();
									pipeline.channel = udp;
									pipeline.local = udp.local;
									
									SelectionKey selectionKey = connection.tcp().accept(selector, channel);
									selectionKey.attach(connection);
								} catch(Throwable t) {
								}
								
								continue;
							}
							
							//Must be a UDP read operation.
							try {
								SocketAddress address = udp.readAddressAndBuffer();
								if(address == null) {
									continue;
								}
								
								connection = connections.get(address);
								Packet packet = udp.readPacket(address);
								if(packet == null) {
									continue;
								}
								
								super.firePacketReceived(new PacketReceivedEvent(connection, packet));
							} catch(Throwable t) {
								if(t.getClass() == InvalidReadException.class) {
									super.fireExceptionThrown(t);
								} else {
									close();
								}
								
								continue;
							}
						} catch(Throwable t) {
							if(connection != null) {
								connection.close();
							} else {
								key.channel().close();
							}
							
							super.fireExceptionThrown(t);
						}
					}
				}
			}
		} catch(Throwable t) {
			close();
			
			super.fireExceptionThrown(t);
		}
	}
	
	public void close() {
		if(!closed.getAndSet(true)) {
			synchronized(lock) {
				if(executor != null) {
					executor.shutdownNow();
					executor = null;
				}
				
				try {
					tcp.close();
				} catch(Throwable t) {
					super.fireExceptionThrown(t);
				}
				
				try {
					udp.close();
				} catch(Throwable t) {
					super.fireExceptionThrown(t);
				}
				
				connections.clear();
			}
		}
	}

	public boolean isActive() {
		return tcp.isActive() && udp.isActive();
	}


}
