package nl.u2.netlib;

public abstract class AbstractConnection implements Connection {

	private EndPoint endPoint;
	
	protected AbstractConnection(EndPoint endPoint) {
		this.endPoint = endPoint;
	}

	public EndPoint endPoint() {
		return endPoint;
	}

}
