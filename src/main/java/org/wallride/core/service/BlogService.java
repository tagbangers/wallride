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

package org.wallride.core.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.SecurityUtils;
import com.google.api.services.analytics.Analytics;
import com.google.api.services.analytics.AnalyticsScopes;
import com.google.api.services.analytics.model.GaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.wallride.core.domain.Blog;
import org.wallride.core.domain.GoogleAnalytics;
import org.wallride.core.exception.GoogleAnalyticsException;
import org.wallride.core.exception.ServiceException;
import org.wallride.core.model.GoogleAnalyticsUpdateRequest;
import org.wallride.core.repository.BlogRepository;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

@Service
@Transactional(rollbackFor=Exception.class)
public class BlogService {

	@Resource
	private BlogRepository blogRepository;

	private static Logger logger = LoggerFactory.getLogger(BlogService.class);

	@CacheEvict(value = "blogs", allEntries = true)
	public GoogleAnalytics updateGoogleAnalytics(GoogleAnalyticsUpdateRequest request) {
		byte[] p12;
		try {
			p12 = request.getServiceAccountP12File().getBytes();
		} catch (IOException e) {
			throw new ServiceException(e);
		}

		try {
			PrivateKey privateKey = SecurityUtils.loadPrivateKeyFromKeyStore(
					SecurityUtils.getPkcs12KeyStore(), new ByteArrayInputStream(p12),
					"notasecret", "privatekey", "notasecret");

			HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
			JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

			// Build service account credential.
			Set<String> scopes = new HashSet<>();
			scopes.add(AnalyticsScopes.ANALYTICS_READONLY);

			GoogleCredential credential = new GoogleCredential.Builder().setTransport(httpTransport)
					.setJsonFactory(jsonFactory)
					.setServiceAccountId(request.getServiceAccountId())
					.setServiceAccountScopes(scopes)
					.setServiceAccountPrivateKey(privateKey)
					.build();

			Analytics analytics = new Analytics.Builder(httpTransport, jsonFactory, credential)
					.setApplicationName("WallRide")
					.build();

			GaData gaData = analytics.data().ga()
					.get(request.getProfileId(), "2005-01-01", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), "ga:pageviews")
					.setDimensions(String.format("ga:dimension%d", request.getCustomDimensionIndex()))
					.setMaxResults(1)
					.execute();
			logger.debug("GaData: {}", gaData);
		} catch (GeneralSecurityException e) {
			throw new GoogleAnalyticsException(e);
		} catch (IOException e) {
			throw new GoogleAnalyticsException(e);
		}

		GoogleAnalytics googleAnalytics = new GoogleAnalytics();
		googleAnalytics.setTrackingId(request.getTrackingId());
		googleAnalytics.setProfileId(request.getProfileId());
		googleAnalytics.setCustomDimensionIndex(request.getCustomDimensionIndex());
		googleAnalytics.setServiceAccountId(request.getServiceAccountId());
		googleAnalytics.setServiceAccountP12FileName(request.getServiceAccountP12File().getOriginalFilename());
		googleAnalytics.setServiceAccountP12FileContent(p12);

		Blog blog = blogRepository.findByIdForUpdate(request.getBlogId());
		blog.setGoogleAnalytics(googleAnalytics);

		blog = blogRepository.saveAndFlush(blog);
		return blog.getGoogleAnalytics();
	}

	@CacheEvict(value = "blogs", allEntries = true)
	public GoogleAnalytics deleteGoogleAnalytics(long blogId) {
		Blog blog = blogRepository.findByIdForUpdate(blogId);
		GoogleAnalytics googleAnalytics = blog.getGoogleAnalytics();
		blog.setGoogleAnalytics(null);
		blogRepository.saveAndFlush(blog);
		return googleAnalytics;
	}

	@Cacheable(value = "blogs")
	public Blog getBlogById(long id) {
		return blogRepository.findById(id);
	}
}
