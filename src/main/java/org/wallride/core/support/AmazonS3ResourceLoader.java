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