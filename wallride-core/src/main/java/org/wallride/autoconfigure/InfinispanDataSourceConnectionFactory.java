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

package org.wallride.autoconfigure;

import org.infinispan.persistence.jdbc.configuration.ConnectionFactoryConfiguration;
import org.infinispan.persistence.jdbc.connectionfactory.ConnectionFactory;
import org.infinispan.persistence.jdbc.logging.Log;
import org.infinispan.persistence.spi.PersistenceException;
import org.infinispan.util.logging.LogFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class InfinispanDataSourceConnectionFactory extends ConnectionFactory {

	private static final Log log = LogFactory.getLog(InfinispanDataSourceConnectionFactory.class, Log.class);
	private static final boolean trace = log.isTraceEnabled();

	private DataSource dataSource;

	@Override
	public void start(ConnectionFactoryConfiguration factoryConfiguration, ClassLoader classLoader) throws PersistenceException {
		if (factoryConfiguration instanceof InfinispanDataSourceConnectionFactoryConfiguration) {
			InfinispanDataSourceConnectionFactoryConfiguration configuration = (InfinispanDataSourceConnectionFactoryConfiguration) factoryConfiguration;
			dataSource = configuration.dataSource();
		}
		else {
			throw new PersistenceException("FactoryConfiguration has to be an instance of " +
					"ManagedConnectionFactoryConfiguration");
		}
	}

	@Override
	public void stop() {
	}

	@Override
	public Connection getConnection() throws PersistenceException {
		Connection connection;
		try {
			connection = dataSource.getConnection();
		} catch (SQLException e) {
			log.sqlFailureRetrievingConnection(e);
			throw new PersistenceException("This might be related to https://jira.jboss.org/browse/ISPN-604", e);
		}
		if (trace) {
			log.tracef("Connection checked out: %s", connection);
		}
		return connection;

	}

	@Override
	public void releaseConnection(Connection conn) {
		try {
			if (conn != null) // Could be null if getConnection failed
				conn.close();
		} catch (SQLException e) {
			log.sqlFailureClosingConnection(conn, e);
		}
	}
}
