package org.wallride.support;

import com.amazonaws.services.s3.AmazonS3Client;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

public class AmazonS3ResourceLoader extends FileSystemResourceLoader {
	
	public static final String S3_URL_PREFIX = "s3:";
	
	private AmazonS3Client client;
	
	public AmazonS3ResourceLoader(AmazonS3Client client) {
		this.client = client;
	}
	
	@Override
	public Resource getResource(String location) {
		Assert.notNull(location, "Location must not be null");
		if (location.startsWith(S3_URL_PREFIX)) {
			String path = location.substring(S3_URL_PREFIX.length());
			int pos = path.indexOf('/');
			String bucketName = "";
			String key = "";
			if (pos != -1) {
				bucketName = path.substring(0, pos);
				key = path.substring(pos + 1);
			}
			else {
				bucketName = path;
			}
			
			return new AmazonS3Resource(client, bucketName, key);
		}
		return super.getResource(location);
	}
}