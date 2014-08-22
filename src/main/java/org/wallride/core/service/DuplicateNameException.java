package org.wallride.core.service;

public class DuplicateNameException extends ServiceException {

	private String name;

	public DuplicateNameException(String name) {
		super();
		this.name = name;
	}

	public DuplicateNameException(String name, Throwable cause) {
		super(cause);
		this.name = name;
	}

	public DuplicateNameException(String name, String message) {
		super(message);
		this.name = name;
	}

	public DuplicateNameException(String name, String message, Throwable cause) {
		super(message, cause);
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
