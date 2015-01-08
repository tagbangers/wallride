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

import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.s3.model.DeleteObjectsRequest.KeyVersion;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public abstract class AmazonS3ResourceUtils {

	private static Logger logger = LoggerFactory.getLogger(AmazonS3ResourceUtils.class);

	public static void writeByteArray(byte[] bytes, Resource resource, String contentType) throws IOException {
		long startTime = System.currentTimeMillis();
		if (resource instanceof AmazonS3Resource) {
			AmazonS3Resource amazonS3Resource = (AmazonS3Resource) resource;

			ObjectMetadata metadata = new ObjectMetadata();
			metadata.setContentType(contentType);
			metadata.setContentLength(bytes.length);
			metadata.setLastModified(new Date());

			PutObjectRequest request = new PutObjectRequest(
					amazonS3Resource.getBucketName(),
					amazonS3Resource.getKey(),
					new ByteArrayInputStream(bytes),
					metadata);

			amazonS3Resource.getClient().putObject(request);
		}
		else {
			FileUtils.writeByteArrayToFile(resource.getFile(), bytes);
		}
		long stopTime = System.currentTimeMillis();
		logger.debug("Resource has benn saved: resource [{}], time [{}]", resource, stopTime - startTime);
	}

	public static void writeFile(File file, Resource resource) throws IOException {
		long startTime = System.currentTimeMillis();
		if (resource instanceof AmazonS3Resource) {
			AmazonS3Resource amazonS3Resource = (AmazonS3Resource) resource;
			amazonS3Resource.getClient().putObject(amazonS3Resource.getBucketName(), amazonS3Resource.getKey(), file);
		}
		else {
			FileUtils.copyFile(file, resource.getFile());
		}
		long stopTime = System.currentTimeMillis();
		logger.debug("Resource has benn saved: resource [{}], time [{}]", resource, stopTime - startTime);
	}

	public static void writeMultipartFile(MultipartFile file, Resource resource) throws IOException {
		long startTime = System.currentTimeMillis();
		if (resource instanceof AmazonS3Resource) {
			AmazonS3Resource amazonS3Resource = (AmazonS3Resource) resource;

			ObjectMetadata metadata = new ObjectMetadata();
			metadata.setContentType(file.getContentType());
			metadata.setContentLength(file.getSize());
			metadata.setLastModified(new Date());

			PutObjectRequest request = new PutObjectRequest(
					amazonS3Resource.getBucketName(),
					amazonS3Resource.getKey(),
					file.getInputStream(),
					metadata);

			amazonS3Resource.getClient().putObject(request);
		}
		else {
			FileUtils.copyInputStreamToFile(file.getInputStream(), resource.getFile());
		}
		long stopTime = System.currentTimeMillis();
		logger.debug("Resource has benn saved: resource [{}], time [{}]", resource, stopTime - startTime);
	}

	public static void delete(Resource resource) throws IOException {
		if (resource instanceof AmazonS3Resource) {
			AmazonS3Resource amazonS3Resource = (AmazonS3Resource) resource;
			
			List<KeyVersion> keys = new ArrayList<>();
			ObjectListing list = amazonS3Resource.getClient().listObjects(amazonS3Resource.getBucketName(), amazonS3Resource.getKey());
			List<S3ObjectSummary> summaries = list.getObjectSummaries();
			for (S3ObjectSummary summary : summaries) {
				keys.add(new KeyVersion(summary.getKey()));
			}
			
			if (!keys.isEmpty()) {
				DeleteObjectsRequest request = new DeleteObjectsRequest(amazonS3Resource.getBucketName());
				request.setKeys(keys);
				amazonS3Resource.getClient().deleteObjects(request);
			}
		}
		else if (resource instanceof FileSystemResource) {
			FileSystemResource fileSystemResource = (FileSystemResource) resource;
			FileUtils.deleteQuietly(fileSystemResource.getFile());
		}
		else {
			throw new IllegalArgumentException();
		}
	}
}
