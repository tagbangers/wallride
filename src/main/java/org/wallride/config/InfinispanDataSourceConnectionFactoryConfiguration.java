package org.wallride.config;

import org.infinispan.commons.configuration.BuiltBy;
import org.infinispan.persistence.jdbc.configuration.ConnectionFactoryConfiguration;
import org.infinispan.persistence.jdbc.connectionfactory.ConnectionFactory;

import javax.sql.DataSource;

@BuiltBy(InfinispanDataSourceConnectionFactoryConfigurationBuilder.class)
public class InfinispanDataSourceConnectionFactoryConfiguration implements ConnectionFactoryConfiguration {

	private final DataSource dataSource;

	InfinispanDataSourceConnectionFactoryConfiguration(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public DataSource dataSource() {
		return dataSource;
	}

	@Override
	public Class<? extends ConnectionFactory> connectionFactoryClass() {
		return InfinispanDataSourceConnectionFactory.class;
	}

	@Override
	public String toString() {
		return "InfinispanDataSourceConnectionFactoryConfiguration [dataSource=" + dataSource + "]";
	}
}
