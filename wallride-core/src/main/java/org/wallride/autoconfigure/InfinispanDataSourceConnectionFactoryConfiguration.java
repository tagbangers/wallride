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
