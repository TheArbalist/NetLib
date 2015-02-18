package nl.u2.netlib;

import java.nio.ByteBuffer;

public interface EndPoint {

	public void addListener(SessionListener listener);
	
	public void removeListener(SessionListener listener);
	
	public void fireSessionReceived(Session session, ByteBuffer buffer);
	
	public void fireSessionConnected(Session session);
	
	public void fireSessionDisconnected(Session session);
	
}
