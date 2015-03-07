package nl.u2.netlib.listener.event;

import nl.u2.netlib.Connection;
import nl.u2.netlib.packet.Packet;

public final class PacketReceivedEvent extends ConnectionEvent {

	private Packet packet;
	
	public PacketReceivedEvent(Connection connection, Packet packet) {
		super(connection);
		
		this.packet = packet;
	}
	
	public Packet packet() {
		return packet;
	}

}
