package org.wallride.web.support;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
@SuppressWarnings("serial")
public class HttpNotFoundException extends RuntimeException {

	public HttpNotFoundException() {
		super();
	}

	public HttpNotFoundException(Throwable cause) {
		super(cause);
	}

	public HttpNotFoundException(String message) {
		super(message);
	}

	public HttpNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
}
