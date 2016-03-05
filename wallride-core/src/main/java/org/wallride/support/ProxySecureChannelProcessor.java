/*
 * Copyright 2014 Tagbangers, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wallride.support;

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
