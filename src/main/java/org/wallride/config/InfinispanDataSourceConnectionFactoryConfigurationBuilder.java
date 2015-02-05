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

package org.wallride.config;

import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.persistence.jdbc.configuration.AbstractJdbcStoreConfigurationBuilder;
import org.infinispan.persistence.jdbc.configuration.AbstractJdbcStoreConfigurationChildBuilder;
import org.infinispan.persistence.jdbc.configuration.ConnectionFactoryConfigurationBuilder;

import javax.sql.DataSource;

public class InfinispanDataSourceConnectionFactoryConfigurationBuilder<S extends AbstractJdbcStoreConfigurationBuilder<?, S>> extends AbstractJdbcStoreConfigurationChildBuilder<S>
		implements ConnectionFactoryConfigurationBuilder<InfinispanDataSourceConnectionFactoryConfiguration> {

	public InfinispanDataSourceConnectionFactoryConfigurationBuilder(AbstractJdbcStoreConfigurationBuilder<?, S> builder) {
		super(builder);
	}

	private DataSource dataSource;

	public void dataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	public void validate() {

	}

	@Override
	public void validate(GlobalConfiguration globalConfiguration) {

	}

	@Override
	public InfinispanDataSourceConnectionFactoryConfiguration create() {
		return new InfinispanDataSourceConnectionFactoryConfiguration(dataSource);
	}

	@Override
	public InfinispanDataSourceConnectionFactoryConfigurationBuilder<S> read(InfinispanDataSourceConnectionFactoryConfiguration template) {
		this.dataSource = template.dataSource();
		return this;
	}
}
