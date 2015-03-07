package nl.u2.netlib;

public abstract class AbstractPipeline implements Pipeline {

	private Connection connection;
	
	protected AbstractPipeline(Connection connection) {
		this.connection = connection;
	}
	
	public Connection connection() {
		return connection;
	}

}
