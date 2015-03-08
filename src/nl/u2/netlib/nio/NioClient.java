package nl.u2.netlib.nio;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import nl.u2.netlib.AbstractEndPoint;
import nl.u2.netlib.Client;
import nl.u2.netlib.EndPoint;
import nl.u2.netlib.Pipeline;
import nl.u2.netlib.exception.InvalidReadException;
import nl.u2.netlib.listener.event.ConnectionEstablishedEvent;
import nl.u2.netlib.listener.event.PacketReceivedEvent;
import nl.u2.netlib.packet.Packet;
import nl.u2.netlib.packet.PacketBuilder;
import nl.u2.netlib.packet.PacketHeaders;

public class NioClient extends AbstractEndPoint implements Client, Runnable {

	private static final int DEFAULT_BUFFER_SIZE = 1024;
	
	private final Object lock = new Object();
	private final AtomicBoolean closed = new AtomicBoolean();

	private ExecutorService executor;
	private Selector selector;
	private int emptySelects;

	private NioTcpPipeline tcp;
	private NioUdpPipeline udp;
	private NioUdpChannel udpChannel;
	
	public NioClient() {
		this(DEFAULT_BUFFER_SIZE);
	}
	
	public NioClient(int bufferSize) {
		try {
			selector = Selector.open();
		} catch(Throwable t) {
			super.fireExceptionThrown(t);
		}
		
		tcp = new NioTcpPipeline(this, bufferSize);
		udp = new NioUdpPipeline(this);
		udpChannel = new NioUdpChannel(bufferSize);
	}
	
	@Override
	public void connect(String host, int tcpPort, int udpPort) throws Exception {
		close();
		
		synchronized(lock) {
			try {
				tcp.connect(selector, new InetSocketAddress(host, tcpPort));
				InetSocketAddress address = new InetSocketAddress(host, udpPort);
				udpChannel.connect(selector, address);
				
				udp.local = udpChannel.local;
				udp.remoteAddress(address);
				udp.channel = udpChannel;
				
				executor = Executors.newSingleThreadExecutor();
				executor.execute(this);
				
				closed.set(false);
				
				tcp.write(new PacketBuilder().opcode(PacketHeaders.PACKET_UDP_REGISTRATION).writeInt(udpChannel.local.getPort()).toPacket());
				super.fireConnectionEstablished(new ConnectionEstablishedEvent(this));
			} catch(Exception e) {
				close();
				
				throw e;
			}
		}
	}
	
	@Override
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
						
						if(key.attachment() == tcp) {
							if(key.isReadable()) {
								try {
									while(true) {
										Packet packet = tcp.readPacket();
										if (packet == null) {
											break;
										}
	
										super.firePacketReceived(new PacketReceivedEvent(this, packet));
									}
								} catch(Throwable t) {
									if(t.getClass() == InvalidReadException.class) {
										super.fireExceptionThrown(t);
									} else {
										close();
									}
									
									continue;
								}
							}
							
							if(key.isWritable()) {
								try {
									tcp.writeBuffer();
								} catch(Throwable t) {
									close();
									continue;
								}
							}
						} else {
							try {
								SocketAddress address = udpChannel.readAddressAndBuffer();
								if(address == null) {
									continue;
								}
								
								Packet packet = udpChannel.readPacket(address);
								if(packet == null) {
									continue;
								}
								
								super.firePacketReceived(new PacketReceivedEvent(this, packet));
							} catch(Throwable t) {
								if(t.getClass() == InvalidReadException.class) {
									super.fireExceptionThrown(t);
								} else {
									close();
								}
								
								continue;
							}
						}
					}
					
				}
			}
		} catch(Throwable t) {
			close();
			
			super.fireExceptionThrown(t);
		}
	}
	
	@Override
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
					udpChannel.close();
				} catch(Throwable t) {
					super.fireExceptionThrown(t);
				}
			}
		}
	}
	
	@Override
	public Pipeline tcp() {
		return tcp;
	}
	
	@Override
	public Pipeline udp() {
		return udp;
	}
	
	@Override
	public boolean isActive() {
		return tcp.isActive() && udp.isActive();
	}

	@Override
	public EndPoint endPoint() {
		return this;
	}

}
