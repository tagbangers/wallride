package org.wallride.core.service;

public class EmailNotFoundException extends ServiceException {

	public EmailNotFoundException() {
		super();
	}

	public EmailNotFoundException(Throwable cause) {
		super(cause);
	}

	public EmailNotFoundException(String message) {
		super(message);
	}

	public EmailNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
}
