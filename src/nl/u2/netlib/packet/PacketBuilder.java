package nl.u2.netlib.packet;

import java.net.SocketAddress;

public final class PacketBuilder {
	
	private static final int DEFAULT_SIZE = 32;
	
	private SocketAddress address;
	private int opcode;
	
	private int length;
	private byte[] payload;

	public PacketBuilder() {
		this(DEFAULT_SIZE);
	}

	public PacketBuilder(int capacity) {
		payload = new byte[capacity];
	}
	
	public PacketBuilder address(SocketAddress address) {
		this.address = address;
		return this;
	}
	
	public PacketBuilder writeBytes(byte[] data) {
		return writeBytes(data, 0, data.length);
	}

	public PacketBuilder writeBytes(byte[] data, int offset, int len) {
		int newLength = length + len;
		ensureCapacity(newLength);
		System.arraycopy(data, offset, payload, length, len);
		length = newLength;
		return this;
	}

	public PacketBuilder writeBoolean(boolean b) {
		return writeByte((byte) (b ? 1 : 0));
	}
	
	public PacketBuilder writeByte(byte val) {
		return writeByte(val, true);
	}

	private PacketBuilder writeByte(byte val, boolean checkCapacity) {
		if (checkCapacity) {
			ensureCapacity(length + 1);
		}
		payload[length++] = val;
		return this;
	}

	public PacketBuilder writeShort(int val) {
		ensureCapacity(length + 2);
		writeByte((byte) (val >> 8), false);
		writeByte((byte) val, false);
		return this;
	}
	
	public PacketBuilder writeInt(int val) {
		ensureCapacity(length + 4);
		writeByte((byte) (val >> 24), false);
		writeByte((byte) (val >> 16), false);
		writeByte((byte) (val >> 8), false);
		writeByte((byte) val, false);
		return this;
	}

	public PacketBuilder writeLong(long val) {
		writeInt((int) (val >> 32));
		writeInt((int) (val & -1L));
		return this;
	}

	public PacketBuilder writeString(String s) {
		byte[] data = s.getBytes();
		writeShort(data.length);
		writeBytes(data, 0, data.length);
		return this;
	}
	
	public PacketBuilder opcode(int opcode) {
		this.opcode = opcode;
		return this;
	}

	public int length() {
		return length;
	}
	
	public byte[] data() {
		return payload;
	}

	public Packet toPacket() {
		byte[] data = new byte[length];
		System.arraycopy(payload, 0, data, 0, length);
		return new Packet(data, opcode, address);
	}
	
	private void ensureCapacity(int minimumCapacity) {
		if (minimumCapacity >= payload.length)
			expandCapacity(minimumCapacity);
	}

	private void expandCapacity(int minimumCapacity) {
		int newCapacity = (payload.length + 1) * 2;
		if (newCapacity < 0) {
			newCapacity = Integer.MAX_VALUE;
		} else if (minimumCapacity > newCapacity) {
			newCapacity = minimumCapacity;
		}
		byte[] newPayload = new byte[newCapacity];
		try {
			while (length > payload.length)
				length--;
			System.arraycopy(payload, 0, newPayload, 0, length);
		} catch (Exception e) {
		}
		payload = newPayload;
	}
}
