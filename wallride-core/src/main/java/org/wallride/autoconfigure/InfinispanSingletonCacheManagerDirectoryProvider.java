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

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockFactory;
import org.hibernate.search.backend.BackendFactory;
import org.hibernate.search.cfg.Environment;
import org.hibernate.search.engine.service.spi.ServiceManager;
import org.hibernate.search.indexes.spi.DirectoryBasedIndexManager;
import org.hibernate.search.spi.BuildContext;
import org.hibernate.search.store.spi.DirectoryHelper;
import org.hibernate.search.store.spi.LockFactoryCreator;
import org.infinispan.Cache;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.hibernate.search.impl.AsyncDeleteExecutorService;
import org.infinispan.hibernate.search.impl.LoggerFactory;
import org.infinispan.hibernate.search.logging.Log;
import org.infinispan.hibernate.search.spi.InfinispanDirectoryProvider;
import org.infinispan.hibernate.search.spi.InfinispanIntegration;
import org.infinispan.hibernate.search.util.configuration.impl.ConfigurationParseHelper;
import org.infinispan.lucene.FileCacheKey;
import org.infinispan.lucene.directory.DirectoryBuilder;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.remoting.transport.Address;
import org.infinispan.util.concurrent.WithinThreadExecutor;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

public class InfinispanSingletonCacheManagerDirectoryProvider implements org.hibernate.search.store.DirectoryProvider<Directory> {

	private static final Log log = LoggerFactory.make();

	private ServiceManager serviceManager;
	private String directoryProviderName;

	private String metadataCacheName;
	private String dataCacheName;
	private String lockingCacheName;
	private Integer chunkSize;

	private Directory directory;

//	private EmbeddedCacheManager cacheManager;
	static EmbeddedCacheManager cacheManager; // #### Attension ###

	private AsyncDeleteExecutorService deletesExecutor;

	private boolean writeFileListAsync;

	private LockFactory indexWriterLockFactory;
	private final int affinityId;
	private boolean isAsync;


	public InfinispanSingletonCacheManagerDirectoryProvider(int affinityId) {
		this.affinityId = affinityId;
	}

	public InfinispanSingletonCacheManagerDirectoryProvider() {
		this.affinityId = -1;
	}

	@Override
	public void initialize(String directoryProviderName, Properties properties, BuildContext context) {
		this.directoryProviderName = directoryProviderName;
		this.serviceManager = context.getServiceManager();
//		this.cacheManager = serviceManager.requestService(CacheManagerService.class).getEmbeddedCacheManager();
		metadataCacheName = InfinispanIntegration.getMetadataCacheName(properties);
		dataCacheName = InfinispanIntegration.getDataCacheName(properties);
		lockingCacheName = InfinispanIntegration.getLockingCacheName(properties);
		//Let it return null if it's not set, so that we can avoid applying any override.
		chunkSize = ConfigurationParseHelper.getIntValue(properties, "chunk_size");
		writeFileListAsync = getWriteFileListAsync(properties);

		//Only override the default Infinispan LockDirectory if an explicit option is set:
		if (configurationExplicitlySetsLockFactory(properties)) {
			Path verifiedIndexDir = null;
			if (isNativeLockingStrategy(properties)) {
				verifiedIndexDir = DirectoryHelper.getVerifiedIndexPath(
						directoryProviderName,
						properties,
						true
				);
			}
			indexWriterLockFactory = getLockFactory(verifiedIndexDir, properties);
		}
		this.isAsync = !BackendFactory.isConfiguredAsSync(properties);
	}

	private LockFactory getLockFactory(Path indexDir, Properties properties) {
		try {
			return serviceManager.requestService(LockFactoryCreator.class).createLockFactory(indexDir, properties);
		} finally {
			serviceManager.releaseService(LockFactoryCreator.class);
		}
	}

	private boolean getWriteFileListAsync(Properties properties) {
		return ConfigurationParseHelper.getBooleanValue(
				properties,
				InfinispanIntegration.WRITE_METADATA_ASYNC,
				false
		);
	}

	/**
	 * @param dirConfiguration the properties representing the configuration for this index
	 * @return {@code true} if the configuration contains an override for the locking_strategy
	 */
	private boolean configurationExplicitlySetsLockFactory(Properties dirConfiguration) {
		return dirConfiguration.getProperty(Environment.LOCKING_STRATEGY) != null;
	}

	private boolean isNativeLockingStrategy(Properties dirConfiguration) {
		return "native".equals(dirConfiguration.getProperty(Environment.LOCKING_STRATEGY));
	}

	@Override
	public void start(DirectoryBasedIndexManager indexManager) {
		log.debug("Starting InfinispanDirectory");
		deletesExecutor = getDeleteOperationsExecutor();
		validateCacheManagerConfiguration();
		cacheManager.startCaches(metadataCacheName, dataCacheName, lockingCacheName);
		Cache<?, ?> metadataCache = cacheManager.getCache(metadataCacheName);
		Cache<?, ?> dataCache = cacheManager.getCache(dataCacheName);
		Cache<?, ?> lockingCache = cacheManager.getCache(lockingCacheName);
		org.infinispan.lucene.directory.BuildContext directoryBuildContext = DirectoryBuilder
				.newDirectoryInstance(metadataCache, dataCache, lockingCache, directoryProviderName)
				.writeFileListAsynchronously(writeFileListAsync)
				.deleteOperationsExecutor(isAsync ? new WithinThreadExecutor() : deletesExecutor.getExecutor());
		if (chunkSize != null) {
			directoryBuildContext.chunkSize(chunkSize);
		}
		if (indexWriterLockFactory != null) {
			directoryBuildContext.overrideWriteLocker(indexWriterLockFactory);
		}
		if (affinityId >= 0) {
			directoryBuildContext.affinityLocationIntoSegment(affinityId);

		}
		directory = directoryBuildContext.create();
		DirectoryHelper.initializeIndexIfNeeded(directory);
		log.debugf("Initialized Infinispan index: '%s'", directoryProviderName);
	}

	private void validateCacheManagerConfiguration() {
		if (cacheManager.getCacheConfiguration(metadataCacheName) == null) {
			// Synchronizes on the class instead of instance, since multiple caches may have the provider
			// and only 1 can define this cache
			synchronized (InfinispanDirectoryProvider.class) {
				if (cacheManager.getCacheConfiguration(metadataCacheName) == null) {
					log.missingIndexCacheConfiguration(metadataCacheName);
					ConfigurationBuilder builder = new ConfigurationBuilder();
					if (cacheManager.getCacheManagerConfiguration().isClustered()) {
						// Clustered Metadata cache configuration
						builder
								.clustering().cacheMode(CacheMode.REPL_SYNC).remoteTimeout(25000)
								.stateTransfer().awaitInitialTransfer(true).timeout(480000)
								.locking().useLockStriping(false).lockAcquisitionTimeout(10000).concurrencyLevel(500)
						;
					} else {
						builder.simpleCache(true);
					}
					cacheManager.defineConfiguration(metadataCacheName, builder.build());
				}
			}
		}

		if (cacheManager.getCacheConfiguration(dataCacheName) == null) {
			synchronized (InfinispanDirectoryProvider.class) {
				if (cacheManager.getCacheConfiguration(dataCacheName) == null) {
					log.missingIndexCacheConfiguration(dataCacheName);
					ConfigurationBuilder builder = new ConfigurationBuilder();
					if (cacheManager.getCacheManagerConfiguration().isClustered()) {
						// Clustered Metadata cache configuration
						builder
								.clustering().cacheMode(CacheMode.DIST_SYNC).remoteTimeout(25000)
								.stateTransfer().awaitInitialTransfer(true).timeout(480000)
								.locking().useLockStriping(false).lockAcquisitionTimeout(10000).concurrencyLevel(500)
						;
					} else {
						builder.simpleCache(true);
					}
					cacheManager.defineConfiguration(dataCacheName, builder.build());
				}
			}
		}

		if (cacheManager.getCacheConfiguration(lockingCacheName) == null) {
			synchronized (InfinispanDirectoryProvider.class) {
				if (cacheManager.getCacheConfiguration(lockingCacheName) == null) {
					log.missingIndexCacheConfiguration(lockingCacheName);
					ConfigurationBuilder builder = new ConfigurationBuilder();
					if (cacheManager.getCacheManagerConfiguration().isClustered()) {
						// Clustered Metadata cache configuration
						builder
								.clustering().cacheMode(CacheMode.REPL_SYNC).remoteTimeout(25000)
								.stateTransfer().awaitInitialTransfer(true).timeout(480000)
								.locking().useLockStriping(false).lockAcquisitionTimeout(10000).concurrencyLevel(500)
						;
					} else {
						builder.simpleCache(true);
					}
					cacheManager.defineConfiguration(lockingCacheName, builder.build());
				}
			}
		}
	}

	private AsyncDeleteExecutorService getDeleteOperationsExecutor() {
		return serviceManager.requestService(AsyncDeleteExecutorService.class);
	}

	public int pendingDeleteTasks() {
		return isAsync ? 0 : deletesExecutor.getActiveTasks();
	}

	@Override
	public void stop() {
		deletesExecutor.closeAndFlush();
		serviceManager.releaseService(AsyncDeleteExecutorService.class);
		try {
			directory.close();
		} catch (IOException e) {
			log.unableToCloseLuceneDirectory(directory, e);
		}
//		serviceManager.releaseService(CacheManagerService.class);
		cacheManager.stop(); // #### Attension ###
		log.debug("Stopped InfinispanDirectory");
	}

	@Override
	public Directory getDirectory() {
		return directory;
	}

	public EmbeddedCacheManager getCacheManager() {
		return cacheManager;
	}

	public Address getLockOwner(String indexName, int affinityId, String lockName) {
		FileCacheKey fileCacheKey = new FileCacheKey(indexName, lockName, affinityId);
		Cache<?, Address> lockCache = cacheManager.getCache(lockingCacheName);
		Address address = lockCache.get(fileCacheKey);
		log.debugf("Lock owner for %s: %s", fileCacheKey, address);
		return address;
	}

}
