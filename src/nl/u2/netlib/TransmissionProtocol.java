package nl.u2.netlib;

/**
 * All protocols that NetLib currently support:
 * -TCP
 * -UDP
 * 
 * @author U2ForMe
 *
 */
public enum TransmissionProtocol {

	/**
	 * TCP is reliable. Your packets are guaranteed to arrive.
	 * 
	 * Benchmarks:
	 * NIO:
	 * 	~50k packets consisting of 4 bytes per second
	 * UDP:
	 * 	~40k packets consisting of 4 bytes per second
	 */
	TCP,
	
	/**
	 * UDP is less. Your packets aren't guaranteed to arrive. And if they arrive,
	 * They don't have to be delivered in the order you sent them. UDP is, however,
	 * faster than TCP.
	 * 
	 * Benchmarks:
	 * NIO:
	 * 	~70k packets consisting of 4 bytes per second
	 * UDP:
	 * 	~60k packets consisting of 4 bytes per second
	 */
	UDP;
	
}
