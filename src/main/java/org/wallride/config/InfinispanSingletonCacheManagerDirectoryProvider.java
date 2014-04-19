package org.wallride.config;

import org.apache.lucene.store.Directory;
import org.hibernate.search.engine.ServiceManager;
import org.hibernate.search.indexes.impl.DirectoryBasedIndexManager;
import org.hibernate.search.infinispan.InfinispanIntegration;
import org.hibernate.search.infinispan.logging.impl.Log;
import org.hibernate.search.spi.BuildContext;
import org.hibernate.search.store.DirectoryProvider;
import org.hibernate.search.store.impl.DirectoryProviderHelper;
import org.hibernate.search.util.configuration.impl.ConfigurationParseHelper;
import org.hibernate.search.util.logging.impl.LoggerFactory;
import org.infinispan.Cache;
import org.infinispan.lucene.directory.DirectoryBuilder;
import org.infinispan.manager.EmbeddedCacheManager;

import java.io.IOException;
import java.util.Properties;

public class InfinispanSingletonCacheManagerDirectoryProvider implements DirectoryProvider<Directory> {

	private static final Log log = LoggerFactory.make(Log.class);

	private ServiceManager serviceManager;
	private String directoryProviderName;

	private String metadataCacheName;
	private String dataCacheName;
	private String lockingCacheName;
	private Integer chunkSize;

	private Directory directory;

//	private EmbeddedCacheManager cacheManager;
	static EmbeddedCacheManager cacheManager; // #### Attension ###

	@Override
	public void initialize(String directoryProviderName, Properties properties, BuildContext context) {
		this.directoryProviderName = directoryProviderName;
		this.serviceManager = context.getServiceManager();
//		this.cacheManager = serviceManager.requestService(CacheManagerServiceProvider.class, context);
		metadataCacheName = InfinispanIntegration.getMetadataCacheName(properties);
		dataCacheName = InfinispanIntegration.getDataCacheName(properties);
		lockingCacheName = InfinispanIntegration.getLockingCacheName(properties);
		//Let it return null if it's not set, so that we can avoid applying any override.
		chunkSize = ConfigurationParseHelper.getIntValue(properties, "chunk_size");
	}

	@Override
	public void start(DirectoryBasedIndexManager indexManager) {
		log.debug("Starting InfinispanDirectory");
		cacheManager.startCaches(metadataCacheName, dataCacheName, lockingCacheName);
		Cache<?, ?> metadataCache = cacheManager.getCache(metadataCacheName);
		Cache<?, ?> dataCache = cacheManager.getCache(dataCacheName);
		Cache<?, ?> lockingCache = cacheManager.getCache(lockingCacheName);
		org.infinispan.lucene.directory.BuildContext directoryBuildContext = DirectoryBuilder
				.newDirectoryInstance(metadataCache, dataCache, lockingCache, directoryProviderName);
		if (chunkSize != null) {
			directoryBuildContext.chunkSize(chunkSize.intValue());
		}
		directory = directoryBuildContext.create();
		DirectoryProviderHelper.initializeIndexIfNeeded(directory);
		log.debugf("Initialized Infinispan index: '%s'", directoryProviderName);
	}

	@Override
	public void stop() {
		try {
			directory.close();
		} catch (IOException e) {
			log.unableToCloseLuceneDirectory(directory, e);
		}
//		serviceManager.releaseService(CacheManagerServiceProvider.class);
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
}
