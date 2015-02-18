package nl.u2.netlib.nio;

import java.io.IOException;
import java.nio.ByteBuffer;

import nl.u2.netlib.Pipeline;

public abstract class NioPipeline implements Pipeline {
	
	protected abstract ByteBuffer readBuffer() throws IOException;
	
	protected abstract void writeBuffer() throws IOException;
	
	protected abstract void close();
	
}
