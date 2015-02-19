package nl.u2.netlib.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;

import nl.u2.netlib.TransmissionProtocol;

public class NioUdpPipeline extends NioPipeline {
	
	protected NioDatagramPipeline pipeline;
	protected InetSocketAddress local;
	protected InetSocketAddress remote;
	
	protected NioUdpPipeline() {
	}	
	
	public void write(ByteBuffer buffer) throws IOException {
		NioDatagramPipeline pipeline = this.pipeline;
		if(pipeline == null || remote == null) {
			throw new ClosedChannelException();
		}
		
		pipeline.write(remote, buffer);
	}
	
	@Override
	protected ByteBuffer readBuffer() throws IOException {
		throw new IllegalStateException("Operation not supported.");
	}

	@Override
	protected void writeBuffer() throws IOException {
		throw new IllegalStateException("Operation not supported.");
	}

	@Override
	protected void close() {
		throw new IllegalStateException("Operation not supported.");
	}

	public InetSocketAddress remoteAddress() {
		return remote;
	}

	public InetSocketAddress localAddress() {
		return local;
	}

	public TransmissionProtocol protocol() {
		return TransmissionProtocol.UDP;
	}

}
