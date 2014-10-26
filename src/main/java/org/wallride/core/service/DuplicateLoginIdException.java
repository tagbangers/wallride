package org.wallride.core.service;

public class DuplicateLoginIdException extends ServiceException {

	private String code;

	public DuplicateLoginIdException(String code) {
		super();
		this.code = code;
	}

	public DuplicateLoginIdException(String code, Throwable cause) {
		super(cause);
		this.code = code;
	}

	public DuplicateLoginIdException(String code, String message) {
		super(message);
		this.code = code;
	}

	public DuplicateLoginIdException(String code, String message, Throwable cause) {
		super(message, cause);
		this.code = code;
	}

	public String getCode() {
		return code;
	}
}
