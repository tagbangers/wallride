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

package org.wallride.web.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.tuckey.web.filters.urlrewrite.Conf;
import org.tuckey.web.filters.urlrewrite.UrlRewriteFilter;
import org.tuckey.web.filters.urlrewrite.utils.StringUtils;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class ExtendedUrlRewriteFilter extends UrlRewriteFilter {

	private String confPath;

	private static Logger logger = LoggerFactory.getLogger(UrlRewriteFilter.class);

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		String confPathStr = filterConfig.getInitParameter("confPath");
		confPath = StringUtils.trim(confPathStr);
		super.init(filterConfig);
	}

	@Override
	protected void loadUrlRewriter(FilterConfig filterConfig) throws ServletException {
		try {
			loadUrlRewriterResource(filterConfig.getServletContext());
		} catch(Throwable e) {
			logger.error("Can not read ", e);
			throw new ServletException(e);
		}
	}

	private void loadUrlRewriterResource(ServletContext context) throws IOException {
		ResourceLoader webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(context);
		Resource resource = webApplicationContext.getResource(confPath);

		logger.debug("Loaded urlrewrite.xml from " + resource.getFilename ());

		InputStream inputStream = resource.getInputStream();
		if (inputStream != null) {
			Conf conf = new Conf(context, inputStream, confPath, resource.getFilename(), false);
			checkConf(conf);
		}
	}
}
