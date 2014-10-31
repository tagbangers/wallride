package org.wallride.core.support;

import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.channel.SecureChannelProcessor;
import org.springframework.util.Assert;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Collection;

public class ProxySecureChannelProcessor extends SecureChannelProcessor {

	@Override
	public void decide(FilterInvocation invocation, Collection<ConfigAttribute> config) throws IOException, ServletException {
		Assert.isTrue((invocation != null) && (config != null), "Nulls cannot be provided");

		String forwardedProto = invocation.getHttpRequest().getHeader("X-Forwarded-Proto");
		for (ConfigAttribute attribute : config) {
			if (supports(attribute)) {
				if (forwardedProto != null) {
					if (!forwardedProto.equals("https")) {
						getEntryPoint().commence(invocation.getRequest(), invocation.getResponse());
					}
				} else {
					if (!invocation.getHttpRequest().isSecure()) {
						getEntryPoint().commence(invocation.getRequest(), invocation.getResponse());
					}
				}
			}
		}
	}
}
