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

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.Enumeration;

@WebListener
public class WebListenerHandler implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent event) {
		String prefix = "SYSTEM.";
		Enumeration<String> params = event.getServletContext().getInitParameterNames();
		while (params.hasMoreElements()) {
			String param = params.nextElement();
			String value = event.getServletContext().getInitParameter(param);
			if (param.startsWith(prefix)) {
				System.setProperty(param.substring(prefix.length()), value);
			}
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent event) {
//		// stop CacheManger
//		try {
//			JndiTemplate jndiTemplate = new JndiTemplate();
//			DefaultCacheManager cacheManager = (DefaultCacheManager) jndiTemplate.lookup("cacheManager");
//			cacheManager.stop();
//			jndiTemplate.unbind("cacheManager");
//		}
//		catch (NamingException e) {
//			e.printStackTrace();
//		}
//
//		// clear drivers
//		Enumeration<Driver> drivers = DriverManager.getDrivers();
//		Driver driver = null;
//		while(drivers.hasMoreElements()) {
//			try {
//				driver = drivers.nextElement();
//				DriverManager.deregisterDriver(driver);
//			}
//			catch (SQLException e) {
//				e.printStackTrace();
//			}
//		}
//
//		// MySQL driver leaves around a thread. This static method cleans it up.
//		try {
//			AbandonedConnectionCleanupThread.shutdown();
//		}
//		catch (InterruptedException e) {
//			e.printStackTrace();
//		}
	}
}
