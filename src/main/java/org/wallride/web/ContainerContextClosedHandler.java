package org.wallride.web;

import com.mysql.jdbc.AbandonedConnectionCleanupThread;
import org.infinispan.manager.DefaultCacheManager;
import org.springframework.jndi.JndiTemplate;

import javax.naming.NamingException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

@WebListener
public class ContainerContextClosedHandler implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent event) {
		// nothing to do
	}

	@Override
	public void contextDestroyed(ServletContextEvent event) {
		// stop CacheManger
		try {
			JndiTemplate jndiTemplate = new JndiTemplate();
			DefaultCacheManager cacheManager = (DefaultCacheManager) jndiTemplate.lookup("cacheManager");
			cacheManager.stop();
			jndiTemplate.unbind("cacheManager");
		}
		catch (NamingException e) {
			e.printStackTrace();
		}

		// clear drivers
		Enumeration<Driver> drivers = DriverManager.getDrivers();
		Driver driver = null;
		while(drivers.hasMoreElements()) {
			try {
				driver = drivers.nextElement();
				DriverManager.deregisterDriver(driver);
			}
			catch (SQLException e) {
				e.printStackTrace();
			}
		}

		// MySQL driver leaves around a thread. This static method cleans it up.
		try {
			AbandonedConnectionCleanupThread.shutdown();
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
