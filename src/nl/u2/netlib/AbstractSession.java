package nl.u2.netlib;

public abstract class AbstractSession implements Session {

	private Object attachment;
	
	public <T> void setAttachment(T attachment) {
		this.attachment = attachment;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getAttachment() {
		return (T) attachment;
	}

}
