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
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;
import org.wallride.core.domain.Blog;
import org.wallride.core.domain.GoogleAnalytics;
import org.wallride.core.domain.Post;
import org.wallride.core.repository.PostRepository;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional(rollbackFor=Exception.class)
public class PostService {

	@Inject
	private BlogService blogService;
	@Inject
	private CacheManager cacheManager;
	@Inject
	private PlatformTransactionManager transactionManager;
	@Resource
	private PostRepository postRepository;
	@PersistenceContext
	private EntityManager entityManager;

	private static Logger logger = LoggerFactory.getLogger(PostService.class);

	public List<Post> publishScheduledPosts() {
		logger.info("Starting public posts of the scheduled");

		LocalDateTime now = new LocalDateTime();
		List<Post> posts = postRepository.findByStatusAndDateLessThanEqual(Post.Status.SCHEDULED, now);
		for (Post post : posts) {
			post.setStatus(Post.Status.PUBLISHED);
			postRepository.saveAndFlush(post);
		}

		if (!CollectionUtils.isEmpty(posts)) {
			cacheManager.getCache("articles").clear();
			cacheManager.getCache("pages").clear();
		}

		return posts;
	}

	@Async
	@Transactional(propagation = Propagation.NEVER)
	public void syncGoogleAnalytics() {
		logger.info("Starting sync google analytics");

		Blog blog = blogService.readBlogById(Blog.DEFAULT_ID);
		GoogleAnalytics googleAnalytics = blog.getGoogleAnalytics();
		if (googleAnalytics == null) {
			logger.warn("Configuration of Google Analytics can not be found");
			return;
		}

		try {
			PrivateKey privateKey = SecurityUtils.loadPrivateKeyFromKeyStore(
					SecurityUtils.getPkcs12KeyStore(), new ByteArrayInputStream(blog.getGoogleAnalytics().getServiceAccountP12FileContent()),
					"notasecret", "privatekey", "notasecret");

			HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
			JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

			Set<String> scopes = new HashSet<>();
			scopes.add(AnalyticsScopes.ANALYTICS_READONLY);

			GoogleCredential credential = new GoogleCredential.Builder().setTransport(httpTransport)
					.setJsonFactory(jsonFactory)
					.setServiceAccountId(googleAnalytics.getServiceAccountId())
					.setServiceAccountScopes(scopes)
					.setServiceAccountPrivateKey(privateKey)
					.build();

			Analytics analytics = new Analytics.Builder(httpTransport, jsonFactory, credential)
					.setApplicationName("WallRide")
					.build();

			int startIndex = 1;
			int maxResults = 1000;
			int totalResults = 0;
			do {
				final GaData gaData = analytics.data().ga()
						.get(googleAnalytics.getProfileId(), "2005-01-01", LocalDate.now().toString("yyyy-MM-dd"), "ga:pageviews")
						.setDimensions(String.format("ga:dimension%d", googleAnalytics.getCustomDimensionIndex()))
						.setSort(String.format("-ga:dimension%d", googleAnalytics.getCustomDimensionIndex()))
						.setStartIndex(startIndex)
						.setMaxResults(maxResults)
						.execute();

				TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
				transactionTemplate.setPropagationBehavior(TransactionTemplate.PROPAGATION_REQUIRES_NEW);
				transactionTemplate.execute(new TransactionCallbackWithoutResult() {
					public void doInTransactionWithoutResult(TransactionStatus status) {
						for (List row : gaData.getRows()) {
							long postId = Long.parseLong((String) row.get(0));
							Post post = postRepository.findOne(postId);
							if (post == null) {
								logger.debug("Post not found [{}]", postId);
								continue;
							}
							post.setViews(Long.parseLong((String) row.get(1)));
							post = postRepository.saveAndFlush(post);
						}
					}
				});

				startIndex += maxResults;
				totalResults = gaData.getTotalResults();

			} while (startIndex <= totalResults);
		} catch (GeneralSecurityException e) {
			logger.warn("Failed to synchronize with Google Analytics", e);
			throw new GoogleAnalyticsException(e);
		} catch (IOException e) {
			logger.warn("Failed to synchronize with Google Analytics", e);
			throw new GoogleAnalyticsException(e);
		}
	}

	public Page<Post> readPosts(SearchPostRequest request) {
		Pageable pageable = new PageRequest(0, 10);
		return readPosts(request, pageable);
	}

	public Page<Post> readPosts(SearchPostRequest request, Pageable pageable) {
		return postRepository.search(request, pageable);
	}

	@Cacheable(value = "popularPosts")
	public List<Post> readPopularPosts(LocalDate from, String language, int maxResults) {
		Blog blog = blogService.readBlogById(Blog.DEFAULT_ID);
		GoogleAnalytics googleAnalytics = blog.getGoogleAnalytics();
		if (googleAnalytics == null) {
			logger.warn("Configuration of Google Analytics can not be found");
			return new ArrayList<>();
		}

		try {
			PrivateKey privateKey = SecurityUtils.loadPrivateKeyFromKeyStore(
					SecurityUtils.getPkcs12KeyStore(), new ByteArrayInputStream(blog.getGoogleAnalytics().getServiceAccountP12FileContent()),
					"notasecret", "privatekey", "notasecret");

			HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
			JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

			Set<String> scopes = new HashSet<>();
			scopes.add(AnalyticsScopes.ANALYTICS_READONLY);

			GoogleCredential credential = new GoogleCredential.Builder().setTransport(httpTransport)
					.setJsonFactory(jsonFactory)
					.setServiceAccountId(googleAnalytics.getServiceAccountId())
					.setServiceAccountScopes(scopes)
					.setServiceAccountPrivateKey(privateKey)
					.build();

			Analytics analytics = new Analytics.Builder(httpTransport, jsonFactory, credential)
					.setApplicationName("WallRide")
					.build();

			int startIndex = 1;
			final GaData gaData = analytics.data().ga()
					.get(googleAnalytics.getProfileId(), from.toString("yyyy-MM-dd"), LocalDate.now().toString("yyyy-MM-dd"), "ga:pageviews")
					.setDimensions(String.format("ga:dimension%d", googleAnalytics.getCustomDimensionIndex()))
					.setSort(String.format("-ga:pageviews,-ga:dimension%d", googleAnalytics.getCustomDimensionIndex()))
					.setStartIndex(startIndex)
					.setMaxResults(maxResults)
					.execute();

			List<Post> results = new ArrayList<>();
			List<Long> postIds = new ArrayList<>();
			if (!CollectionUtils.isEmpty(gaData.getRows())) {
				for (List row : gaData.getRows()) {
					postIds.add(Long.parseLong((String) row.get(0)));
				}

				SearchPostRequest searchPostRequest = new SearchPostRequest(language);
				searchPostRequest.setPostIds(postIds);
				searchPostRequest.setStatus(Post.Status.PUBLISHED);
				Page<Post> page = postRepository.search(searchPostRequest, new PageRequest(0, postIds.size()));
				List<Post> posts = new ArrayList<>(page.getContent());
				for (long postId : postIds) {
					Post match = null;
					for (Post post : posts) {
						if (postId == post.getId()) {
							match = post;
							break;
						}
					}
					if (match != null) {
						results.add(match);
						posts.remove(match);
					}
				}
			}

			return results;
		} catch (GeneralSecurityException e) {
			throw new GoogleAnalyticsException(e);
		} catch (IOException e) {
			throw new GoogleAnalyticsException(e);
		}
	}

	public Post readPostById(long id, String language) {
		return postRepository.findById(id, language);
	}
}
