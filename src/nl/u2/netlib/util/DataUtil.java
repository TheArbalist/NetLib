package nl.u2.netlib.util;



public final class DataUtil {

	public static int readInt(byte[] data) {
		if(data.length < 4) {
			throw new IllegalArgumentException("Data must have a minimum length of four bytes.");
		}
		
		return readInt(data, 0);
	}
	
	public static int readInt(byte[] data, int offset) {
		if(data.length < 4) {
			throw new IllegalArgumentException("Data must have a minimum length of four bytes.");
		}
		
		if(data.length - offset < 4) {
			throw new IllegalArgumentException("Offset must be at valid position so that data.length - offset < 4.");
		}
		
		return ((data[offset] & 0xff) << 24) | 
				((data[offset + 1] & 0xff) << 16) | 
				((data[offset + 2] & 0xff) << 8) | 
				(data[offset + 3] & 0xff);
	}
	
	public static void writeInt(byte[] data, int value) {
		if(data.length < 4) {
			throw new IllegalArgumentException("Data must have a minimum length of four bytes.");
		}
		
		writeInt(data, 0, value);
	}
	
	public static void writeInt(byte[] data, int offset, int value) {
		if(data.length < 4) {
			throw new IllegalArgumentException("Data must have a minimum length of four bytes.");
		}
		
		if(data.length - offset < 4) {
			throw new IllegalArgumentException("Offset must be at valid position so that data.length - offset < 4.");
		}
		
		data[offset] = ((byte) (value >> 24));
		data[offset + 1] = ((byte) (value >> 16));
		data[offset + 2] = ((byte) (value >> 8));
		data[offset + 3] = ((byte) value);
	}
	
	public static byte[] writeIntToArray(byte[] data, int value) {
		byte[] newData = new byte[4];
		writeInt(newData, value);
		
		return prepend(data, newData);
	}
	
	public static byte[] prepend(byte[] data, byte[] newData) {
		int length = newData.length;
		byte[] output = new byte[data.length + length];

		
		System.arraycopy(newData, 0, output, 0, length);
		System.arraycopy(data, 0, output, length, data.length);

		return output;
	}
	
	private DataUtil() {
	}
	
}
