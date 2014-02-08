package org.wallride.support;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

public class AmazonS3Resource implements Resource {
	
//	private static Logger logger = LoggerFactory.getLogger(AmazonS3Resource.class); 

	private AmazonS3Client client;
	
	private String bucketName;
	
	private String key;

	ObjectMetadata metadata;

//	private long lastModified;
//
//	private long contentLength;
	
//	private static File cacheRoot;
	
//	static {
//		cacheRoot = new File(System.getProperty("java.io.tmpdir"), AmazonS3Resource.class.getCanonicalName());
//		logger.debug("キャッシュディレクトリとして {} を使用します。", cacheRoot.getAbsolutePath());
//		if (!cacheRoot.exists() && !cacheRoot.mkdirs()) {
//			logger.error("キャッシュディレクトリ {} が作成できません。", cacheRoot.getAbsolutePath());
//			throw new RuntimeException(
//					"Directory that preserves the file for cache cannot be used. " +
//					cacheRoot.getAbsolutePath());
//		}
//	}

	public AmazonS3Resource(AmazonS3Client client, String bucketName, String key) {
		this.client = client;
		this.bucketName = bucketName;
		this.key = key;
	}
	
	public AmazonS3Client getClient() {
		return client;
	}
	
	public String getBucketName() {
		return bucketName;
	}
	
	public String getKey() {
		return key;
	}
	
	@Override
	public InputStream getInputStream() throws IOException {
//		File cache = new File(cacheRoot, bucketName + File.separator + key);
//		if (cache.exists()) {
//			if (lastModified == 0) {
//				lastModified = lastModified();
//			}
//			if (cache.lastModified() > (lastModified + 1000)) {
//				return new FileInputStream(cache);
//			}
//		}
		
		S3Object object = client.getObject(bucketName, key);
		
//		cache.getParentFile().mkdirs();
//		File temp = new File(cache.getParentFile(), '.' + cache.getName() + '.' + RandomStringUtils.randomNumeric(10));
//		FileUtils.writeByteArrayToFile(temp, IOUtils.toByteArray(object.getObjectContent()));
//		cache.delete();
//		FileUtils.moveFile(temp, cache);
//		return new FileInputStream(cache);
		return object.getObjectContent();
	}

	@Override
	public boolean exists() {
		if (metadata != null) {
			return true;
		}
		boolean exists = true;
		try {
			metadata = client.getObjectMetadata(bucketName, key);
		}
		catch (AmazonS3Exception e) {
			exists = false;
		}
		return exists;
	}

	@Override
	public boolean isReadable() {
		return true;
	}

	@Override
	public boolean isOpen() {
		return false;
	}

	@Override
	public URL getURL() throws IOException {
		return null;
	}

	@Override
	public URI getURI() throws IOException {
		return null;
	}

	@Override
	public File getFile() throws IOException {
		throw new IOException();
	}

	@Override
	public long contentLength() throws IOException {
		if (metadata != null) {
			return metadata.getContentLength();
		}
		try {
			metadata = client.getObjectMetadata(bucketName, key);
		}
		catch (AmazonS3Exception e) {
			throw new IOException(e);
		}
		return metadata.getContentLength();
	}

	@Override
	public long lastModified() throws IOException {
		if (metadata != null) {
			return metadata.getLastModified().getTime();
		}
		try {
			metadata = client.getObjectMetadata(bucketName, key);
		}
		catch (AmazonS3Exception e) {
			throw new IOException(e);
		}
		return metadata.getLastModified().getTime();
	}
	
	@Override
	public Resource createRelative(String relativePath) throws IOException {
		return new AmazonS3Resource(client, bucketName, key + relativePath);
	}

	@Override
	public String getFilename() {
		return getBucketName() + '/' + getKey();
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public String toString() {
		return getFilename();
	}

	@Override
	public boolean equals(Object obj) {
		return (obj == this ||
				(obj instanceof AmazonS3Resource && getFilename().equals(((AmazonS3Resource) obj).getFilename())));
	}

	@Override
	public int hashCode() {
		return getFilename().hashCode();
	}
}
