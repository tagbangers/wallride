package org.wallride.core.service;

public class NotNullException extends ServiceException {

	public NotNullException() {
		super();
	}

	public NotNullException(Throwable cause) {
		super(cause);
	}

	public NotNullException(String message) {
		super(message);
	}

	public NotNullException(String message, Throwable cause) {
		super(message, cause);
	}
}
