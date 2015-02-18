package nl.u2.netlib.exception;

import java.io.IOException;

public class InvalidReadException extends IOException {

	private static final long serialVersionUID = 1L;
	
	public InvalidReadException(String message) {
		super(message);
	}

}
