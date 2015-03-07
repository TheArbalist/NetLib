package nl.u2.netlib.packet;

import java.net.SocketAddress;
import java.nio.ByteBuffer;

import nl.u2.netlib.TransmissionProtocol;

public final class Packet {
	
	private SocketAddress address;
	private int opcode;
	
	private int length;
	private byte[] payload;
	
	private int caret = 0;
	
	Packet(byte[] data, int opcode, SocketAddress address) {
		payload = data;
		length = data.length;
		
		this.opcode = opcode;
		this.address = address;
	}


	public byte readByte() {
		return payload[caret++];
	}
	
	public boolean readBoolean() {
		return readByte() == 1;
	}

	public short readShort() {
		return (short) ((short) ((payload[caret++] & 0xff) << 8) | (short) (payload[caret++] & 0xff));
	}

	public int readInt() {
		return ((payload[caret++] & 0xff) << 24)
				| ((payload[caret++] & 0xff) << 16)
				| ((payload[caret++] & 0xff) << 8)
				| (payload[caret++] & 0xff);
	}

	public long readLong() {
		return ((long) (payload[caret++] & 0xff) << 56)
				| ((long) (payload[caret++] & 0xff) << 48)
				| ((long) (payload[caret++] & 0xff) << 40)
				| ((long) (payload[caret++] & 0xff) << 32)
				| ((long) (payload[caret++] & 0xff) << 24)
				| ((long) (payload[caret++] & 0xff) << 16)
				| ((long) (payload[caret++] & 0xff) << 8)
				| ((payload[caret++] & 0xff));
	}

	public String readString() {
		byte[] data = new byte[readShort()];
		readBytes(data, 0, data.length);
		
		return new String(data);
	}
	
	public void readBytes(byte[] buffer, int offset, int length) {
		for(int i = 0; i < length; i++) {
			buffer[offset+i] = payload[caret++];
		}
	}
	
	public Packet reset() {
		caret = 0;
		return this;
	}
	
	public Packet skip(int x) {
		caret += x;
		return this;
	}

	public int remaining() {
		return payload.length - caret;
	}
	
	public Packet address(SocketAddress address) {
		this.address = address;
		return this;
	}
	
	public SocketAddress address() {
		return address;
	}
	
	public TransmissionProtocol protocol() {
		return address == null ? TransmissionProtocol.TCP : TransmissionProtocol.UDP;
	}
	
	public int opcode() {
		return opcode;
	}
	
	public int length() {
		return length;
	}

	public byte[] data() {
		return payload;
	}
	
	public byte[] remainingData() {
		byte[] data = new byte[length - caret];
		for(int i = 0;i < data.length;i++) {
			data[i] = payload[i + caret];
		}
		caret += data.length;
		return data;
	}
	
	public static Packet toPacket(byte[] data, int opcode, SocketAddress address) {
		return new Packet(data, opcode, address);
	}
	
	public static Packet toPacket(ByteBuffer buffer, int opcode, SocketAddress address) {
		byte[] data;
		if(buffer.hasArray()) {
			data = buffer.array();
		} else {
			int length = buffer.remaining();
			data = new byte[length];
			buffer.get(data, 0, length);
		}
		
		return new Packet(data, opcode, address);
	}

}
