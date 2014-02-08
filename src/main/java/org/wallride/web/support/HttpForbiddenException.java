package org.wallride.web.support;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
@SuppressWarnings("serial")
public class HttpForbiddenException extends RuntimeException {

	public HttpForbiddenException() {
		super();
	}

	public HttpForbiddenException(Throwable cause) {
		super(cause);
	}

	public HttpForbiddenException(String message) {
		super(message);
	}

	public HttpForbiddenException(String message, Throwable cause) {
		super(message, cause);
	}
}
