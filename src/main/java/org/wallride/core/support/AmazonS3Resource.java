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

package org.wallride.core.support;

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
	
	private AmazonS3Client client;
	
	private String bucketName;
	
	private String key;

	private ObjectMetadata metadata;

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
		S3Object object = client.getObject(bucketName, key);
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
