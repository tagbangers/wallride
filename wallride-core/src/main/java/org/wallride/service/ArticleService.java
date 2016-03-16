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

package org.wallride.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MessageCodesResolver;
import org.wallride.autoconfigure.WallRideCacheConfiguration;
import org.wallride.autoconfigure.WallRideProperties;
import org.wallride.domain.*;
import org.wallride.exception.DuplicateCodeException;
import org.wallride.exception.EmptyCodeException;
import org.wallride.exception.NotNullException;
import org.wallride.exception.ServiceException;
import org.wallride.model.*;
import org.wallride.repository.*;
import org.wallride.support.AuthorizedUser;
import org.wallride.support.CodeFormatter;
import org.wallride.web.controller.admin.article.CustomFieldValueEditForm;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Transactional(rollbackFor=Exception.class)
public class ArticleService {

	@Resource
	private BlogService blogService;

	@Resource
	private PostRepository postRepository;

	@Resource
	private ArticleRepository articleRepository;

	@Resource
	private TagRepository tagRepository;

	@Resource
	private MediaRepository mediaRepository;

	@Inject
	private MessageCodesResolver messageCodesResolver;

	@Inject
	private PlatformTransactionManager transactionManager;

	@Inject
	private WallRideProperties wallRideProperties;

	@PersistenceContext
	private EntityManager entityManager;

	private static Logger logger = LoggerFactory.getLogger(ArticleService.class);

	@CacheEvict(value = WallRideCacheConfiguration.ARTICLE_CACHE, allEntries = true)
	public Article createArticle(ArticleCreateRequest request, Post.Status status, AuthorizedUser authorizedUser) {
		LocalDateTime now = LocalDateTime.now();

		String code = request.getCode();
		if (code == null) {
			try {
				code = new CodeFormatter().parse(request.getTitle(), LocaleContextHolder.getLocale());
			} catch (ParseException e) {
				throw new ServiceException(e);
			}
		}
		if (!StringUtils.hasText(code)) {
			if (!status.equals(Post.Status.DRAFT)) {
				throw new EmptyCodeException();
			}
		}

		if (!status.equals(Post.Status.DRAFT)) {
			Post duplicate = postRepository.findOneByCodeAndLanguage(request.getCode(), request.getLanguage());
			if (duplicate != null) {
				throw new DuplicateCodeException(request.getCode());
			}
		}

		Article article = new Article();

		if (!status.equals(Post.Status.DRAFT)) {
			article.setCode(code);
			article.setDraftedCode(null);
		}
		else {
			article.setCode(null);
			article.setDraftedCode(code);
		}

		Media cover = null;
		if (request.getCoverId() != null) {
			cover = entityManager.getReference(Media.class, request.getCoverId());
		}
		article.setCover(cover);
		article.setTitle(request.getTitle());
		article.setBody(request.getBody());

		article.setAuthor(entityManager.getReference(User.class, authorizedUser.getId()));

		LocalDateTime date = request.getDate();
		if (Post.Status.PUBLISHED.equals(status)) {
			if (date == null) {
				date = now;
			}
			else if (date.isAfter(now)) {
				status = Post.Status.SCHEDULED;
			}
		}
		article.setDate(date);
		article.setStatus(status);
		article.setLanguage(request.getLanguage());

		article.getCategories().clear();
		for (long categoryId : request.getCategoryIds()) {
			article.getCategories().add(entityManager.getReference(Category.class, categoryId));
		}

		article.getTags().clear();
		Set<String> tagNames = StringUtils.commaDelimitedListToSet(request.getTags());
		if (!CollectionUtils.isEmpty(tagNames)) {
			for (String tagName : tagNames) {
				Tag tag = tagRepository.findOneForUpdateByNameAndLanguage(tagName, request.getLanguage());
				if (tag == null) {
					tag = new Tag();
					tag.setName(tagName);
					tag.setLanguage(request.getLanguage());
					article.setCreatedAt(now);
					article.setCreatedBy(authorizedUser.toString());
					article.setUpdatedAt(now);
					article.setUpdatedBy(authorizedUser.toString());
					tag = tagRepository.saveAndFlush(tag);
				}
				article.getTags().add(tag);
			}
		}

		article.getRelatedPosts().clear();
		Set<Post> relatedPosts = new HashSet<>();
		for (long relatedId : request.getRelatedPostIds()) {
			relatedPosts.add(entityManager.getReference(Post.class, relatedId));
		}
		article.setRelatedToPosts(relatedPosts);

		Seo seo = new Seo();
		seo.setTitle(request.getSeoTitle());
		seo.setDescription(request.getSeoDescription());
		seo.setKeywords(request.getSeoKeywords());
		article.setSeo(seo);

		List<Media> medias = new ArrayList<>();
		if (StringUtils.hasText(request.getBody())) {
//			Blog blog = blogService.getBlogById(Blog.DEFAULT_ID);
			String mediaUrlPrefix = wallRideProperties.getMediaUrlPrefix();
			Pattern mediaUrlPattern = Pattern.compile(String.format("%s([0-9a-zA-Z\\-]+)", mediaUrlPrefix));
			Matcher mediaUrlMatcher = mediaUrlPattern.matcher(request.getBody());
			while (mediaUrlMatcher.find()) {
				Media media = mediaRepository.findOneById(mediaUrlMatcher.group(1));
				medias.add(media);
			}
		}
		article.setMedias(medias);

		article.setCreatedAt(now);
		article.setCreatedBy(authorizedUser.toString());
		article.setUpdatedAt(now);
		article.setUpdatedBy(authorizedUser.toString());

		article.getCustomFieldValues().clear();
		if (!CollectionUtils.isEmpty(request.getCustomFieldValues())) {
			for (CustomFieldValueEditForm valueForm : request.getCustomFieldValues()) {
				CustomFieldValue value =  new CustomFieldValue();
				value.setCustomField(entityManager.getReference(CustomField.class, valueForm.getCustomFieldId()));
				value.setPost(article);
				if (valueForm.getFieldType().equals(CustomField.FieldType.CHECKBOX)) {
					value.setTextValue(String.join(",", valueForm.getTextValues()));
				} else {
					value.setTextValue(valueForm.getTextValue());
				}
				value.setStringValue(valueForm.getStringValue());
				value.setNumberValue(valueForm.getNumberValue());
				value.setDateValue(valueForm.getDateValue());
				value.setDatetimeValue(valueForm.getDatetimeValue());
				article.getCustomFieldValues().add(value);
			}
		}

		return articleRepository.save(article);
	}

	@CacheEvict(value = WallRideCacheConfiguration.ARTICLE_CACHE, allEntries = true)
	public Article saveArticleAsDraft(ArticleUpdateRequest request, AuthorizedUser authorizedUser) {
		postRepository.lock(request.getId());
		Article article = articleRepository.findOneByIdAndLanguage(request.getId(), request.getLanguage());
		if (!article.getStatus().equals(Post.Status.DRAFT)) {
			Article draft = articleRepository.findOne(ArticleSpecifications.draft(article));
			if (draft == null) {
				ArticleCreateRequest createRequest = new ArticleCreateRequest.Builder()
						.code(request.getCode())
						.coverId(request.getCoverId())
						.title(request.getTitle())
						.body(request.getBody())
						.authorId(request.getAuthorId())
						.date(request.getDate())
						.categoryIds(request.getCategoryIds())
						.tags(request.getTags())
						.seoTitle(request.getSeoTitle())
						.seoDescription(request.getSeoDescription())
						.seoKeywords(request.getSeoKeywords())
						.customFieldValues(new LinkedHashSet<>(request.getCustomFieldValues()))
						.language(request.getLanguage())
						.build();
				draft = createArticle(createRequest, Post.Status.DRAFT, authorizedUser);
				draft.setDrafted(article);
				return articleRepository.save(draft);
			}
			else {
				ArticleUpdateRequest updateRequest = new ArticleUpdateRequest.Builder()
						.id(draft.getId())
						.code(request.getCode())
						.coverId(request.getCoverId())
						.title(request.getTitle())
						.body(request.getBody())
						.authorId(request.getAuthorId())
						.date(request.getDate())
						.categoryIds(request.getCategoryIds())
						.tags(request.getTags())
						.seoTitle(request.getSeoTitle())
						.seoDescription(request.getSeoDescription())
						.seoKeywords(request.getSeoKeywords())
						.customFieldValues(request.getCustomFieldValues())
						.language(request.getLanguage())
						.build();
				return saveArticle(updateRequest, authorizedUser);
			}
		}
		else {
			return saveArticle(request, authorizedUser);
		}
	}

	@CacheEvict(value = WallRideCacheConfiguration.ARTICLE_CACHE, allEntries = true)
	public Article saveArticleAsPublished(ArticleUpdateRequest request, AuthorizedUser authorizedUser) {
		postRepository.lock(request.getId());
		Article article = articleRepository.findOneByIdAndLanguage(request.getId(), request.getLanguage());
		publishArticle(article);
		return saveArticle(request, authorizedUser);
	}

	private Article publishArticle(Article article) {
		Article deleteTarget = getDraftById(article.getId());
		if (deleteTarget != null) {
			articleRepository.delete(deleteTarget);
		}
		article.setDrafted(null);
		article.setStatus(Post.Status.PUBLISHED);
		Article published = articleRepository.save(article);
		return published;
	}

	@CacheEvict(value = WallRideCacheConfiguration.ARTICLE_CACHE, allEntries = true)
	public Article saveArticleAsUnpublished(ArticleUpdateRequest request, AuthorizedUser authorizedUser) {
		postRepository.lock(request.getId());
		Article article = articleRepository.findOneByIdAndLanguage(request.getId(), request.getLanguage());
		unpublishArticle(article);
		return saveArticle(request, authorizedUser);
	}

	private Article unpublishArticle(Article article) {
		Article deleteTarget = getDraftById(article.getId());
		if (deleteTarget != null) {
			articleRepository.delete(deleteTarget);
		}
		article.setDrafted(null);
		article.setStatus(Post.Status.DRAFT);
		Article unpublished = articleRepository.save(article);
		return unpublished;
	}

	@CacheEvict(value = WallRideCacheConfiguration.ARTICLE_CACHE, allEntries = true)
	public Article saveArticle(ArticleUpdateRequest request, AuthorizedUser authorizedUser) {
		postRepository.lock(request.getId());
		Article article = articleRepository.findOneByIdAndLanguage(request.getId(), request.getLanguage());
		LocalDateTime now = LocalDateTime.now();

		String code = request.getCode();
		if (code == null) {
			try {
				code = new CodeFormatter().parse(request.getTitle(), LocaleContextHolder.getLocale());
			} catch (ParseException e) {
				throw new ServiceException(e);
			}
		}
		if (!StringUtils.hasText(code)) {
			if (!article.getStatus().equals(Post.Status.DRAFT)) {
				throw new EmptyCodeException();
			}
		}
		if (!article.getStatus().equals(Post.Status.DRAFT)) {
			Post duplicate = postRepository.findOneByCodeAndLanguage(code, request.getLanguage());
			if (duplicate != null && !duplicate.equals(article)) {
				throw new DuplicateCodeException(request.getCode());
			}
		}

		if (!article.getStatus().equals(Post.Status.DRAFT)) {
			article.setCode(code);
			article.setDraftedCode(null);
		}
		else {
			article.setCode(null);
			article.setDraftedCode(code);
		}

		Media cover = null;
		if (request.getCoverId() != null) {
			cover = entityManager.getReference(Media.class, request.getCoverId());
		}
		article.setCover(cover);
		article.setTitle(request.getTitle());
		article.setBody(request.getBody());

//		User author = null;
//		if (request.getAuthorId() != null) {
//			author = entityManager.getReference(User.class, request.getAuthorId());
//		}
//		article.setAuthor(author);

		LocalDateTime date = request.getDate();
		if (!Post.Status.DRAFT.equals(article.getStatus())) {
			if (date == null) {
				date = now;
			} else if (date.isAfter(now)) {
				article.setStatus(Post.Status.SCHEDULED);
			} else {
				article.setStatus(Post.Status.PUBLISHED);
			}
		}
		article.setDate(date);
		article.setLanguage(request.getLanguage());

		article.getCategories().clear();
		for (long categoryId : request.getCategoryIds()) {
			article.getCategories().add(entityManager.getReference(Category.class, categoryId));
		}

		article.getTags().clear();
		Set<String> tagNames = StringUtils.commaDelimitedListToSet(request.getTags());
		if (!CollectionUtils.isEmpty(tagNames)) {
			for (String tagName : tagNames) {
				Tag tag = tagRepository.findOneForUpdateByNameAndLanguage(tagName, request.getLanguage());
				if (tag == null) {
					tag = new Tag();
					tag.setName(tagName);
					tag.setLanguage(request.getLanguage());
					article.setCreatedAt(now);
					article.setCreatedBy(authorizedUser.toString());
					article.setUpdatedAt(now);
					article.setUpdatedBy(authorizedUser.toString());
					tag = tagRepository.saveAndFlush(tag);
				}
				article.getTags().add(tag);
			}
		}

		article.getRelatedPosts().clear();
		Set<Post> relatedPosts = new HashSet<>();
		for (long relatedId : request.getRelatedPostIds()) {
			relatedPosts.add(entityManager.getReference(Post.class, relatedId));
		}
		article.setRelatedToPosts(relatedPosts);

		Seo seo = new Seo();
		seo.setTitle(request.getSeoTitle());
		seo.setDescription(request.getSeoDescription());
		seo.setKeywords(request.getSeoKeywords());
		article.setSeo(seo);

		List<Media> medias = new ArrayList<>();
		if (StringUtils.hasText(request.getBody())) {
//			Blog blog = blogService.getBlogById(Blog.DEFAULT_ID);
			String mediaUrlPrefix = wallRideProperties.getMediaUrlPrefix();
			Pattern mediaUrlPattern = Pattern.compile(String.format("%s([0-9a-zA-Z\\-]+)", mediaUrlPrefix));
			Matcher mediaUrlMatcher = mediaUrlPattern.matcher(request.getBody());
			while (mediaUrlMatcher.find()) {
				Media media = mediaRepository.findOneById(mediaUrlMatcher.group(1));
				medias.add(media);
			}
		}
		article.setMedias(medias);

		article.setUpdatedAt(now);
		article.setUpdatedBy(authorizedUser.toString());

		SortedSet<CustomFieldValue> fieldValues = new TreeSet<>();
		Map<CustomField, CustomFieldValue> valueMap = new LinkedHashMap<>();
		for (CustomFieldValue value : article.getCustomFieldValues()) {
			valueMap.put(value.getCustomField(), value);
		}

		article.getCustomFieldValues().clear();
		if (!CollectionUtils.isEmpty(request.getCustomFieldValues())) {
			for (CustomFieldValueEditForm valueForm : request.getCustomFieldValues()) {
				CustomField customField = entityManager.getReference(CustomField.class, valueForm.getCustomFieldId());
				CustomFieldValue value = valueMap.get(customField);
				if (value == null) {
					value = new CustomFieldValue();
				}
				value.setCustomField(customField);
				value.setPost(article);
				if (valueForm.getFieldType().equals(CustomField.FieldType.CHECKBOX)) {
					value.setTextValue(String.join(",", valueForm.getTextValues()));
				} else {
					value.setTextValue(valueForm.getTextValue());
				}
				value.setStringValue(valueForm.getStringValue());
				value.setNumberValue(valueForm.getNumberValue());
				value.setDateValue(valueForm.getDateValue());
				value.setDatetimeValue(valueForm.getDatetimeValue());
				fieldValues.add(value);
			}
		}
		article.setCustomFieldValues(fieldValues);

		return articleRepository.save(article);
	}

	@CacheEvict(value = WallRideCacheConfiguration.ARTICLE_CACHE, allEntries = true)
	public Article deleteArticle(ArticleDeleteRequest request, BindingResult result) throws BindException {
		postRepository.lock(request.getId());
		Article article = articleRepository.findOneByIdAndLanguage(request.getId(), request.getLanguage());
		articleRepository.delete(article);
		return article;
	}

	@CacheEvict(value = WallRideCacheConfiguration.ARTICLE_CACHE, allEntries = true)
	public List<Article> bulkPublishArticle(ArticleBulkPublishRequest request, AuthorizedUser authorizedUser) {
		List<Article> articles = new ArrayList<>();
		for (long id : request.getIds()) {
			postRepository.lock(id);
			Article article = articleRepository.findOneByIdAndLanguage(id, request.getLanguage());
			if (article.getStatus() != Post.Status.DRAFT && request.getDate() == null) {
				continue;
			}

			if (!StringUtils.hasText(article.getCode())) {
				throw new NotNullException();
			}
			if (!StringUtils.hasText(article.getTitle())) {
				throw new NotNullException();
			}
			if (!StringUtils.hasText(article.getBody())) {
				throw new NotNullException();
			}

			LocalDateTime now = LocalDateTime.now();
			LocalDateTime date = article.getDate();
			if (request.getDate() != null) {
				date = request.getDate();
			}
			if (date == null) {
				date = now;
			}
			article.setDate(date);
			article.setUpdatedAt(now);
			article.setUpdatedBy(authorizedUser.toString());

			article = publishArticle(article);

			if (article.getDate().isAfter(now)) {
				article.setStatus(Post.Status.SCHEDULED);
			} else {
				article.setStatus(Post.Status.PUBLISHED);
			}
			article = articleRepository.saveAndFlush(article);

			articles.add(article);
		}
		return articles;
	}

	@CacheEvict(value = WallRideCacheConfiguration.ARTICLE_CACHE, allEntries = true)
	public List<Article> bulkUnpublishArticle(ArticleBulkUnpublishRequest request, AuthorizedUser authorizedUser) {
		List<Article> articles = new ArrayList<>();
		for (long id : request.getIds()) {
			postRepository.lock(id);
			Article article = articleRepository.findOneByIdAndLanguage(id, request.getLanguage());
			if (article.getStatus() == Post.Status.DRAFT) {
				continue;
			}

			LocalDateTime now = LocalDateTime.now();
			article.setUpdatedAt(now);
			article.setUpdatedBy(authorizedUser.toString());

			article = unpublishArticle(article);
			articles.add(article);
		}
		return articles;
	}

	@Transactional(propagation=Propagation.NOT_SUPPORTED)
	@CacheEvict(value = WallRideCacheConfiguration.ARTICLE_CACHE, allEntries = true)
	public List<Article> bulkDeleteArticle(ArticleBulkDeleteRequest bulkDeleteRequest, BindingResult result) {
		List<Article> articles = new ArrayList<>();
		for (long id : bulkDeleteRequest.getIds()) {
			final ArticleDeleteRequest deleteRequest = new ArticleDeleteRequest.Builder()
					.id(id)
					.language(bulkDeleteRequest.getLanguage())
					.build();

			final BeanPropertyBindingResult r = new BeanPropertyBindingResult(deleteRequest, "request");
			r.setMessageCodesResolver(messageCodesResolver);

			TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
			transactionTemplate.setPropagationBehavior(TransactionTemplate.PROPAGATION_REQUIRES_NEW);
			Article article = null;
			try {
				article = transactionTemplate.execute(new TransactionCallback<Article>() {
					public Article doInTransaction(TransactionStatus status) {
						try {
							return deleteArticle(deleteRequest, r);
						}
						catch (BindException e) {
							throw new RuntimeException(e);
						}
					}
				});
				articles.add(article);
			}
			catch (Exception e) {
				logger.debug("Errors: {}", r);
				result.addAllErrors(r);
			}
		}
		return articles;
	}

	public List<Long> getArticleIds(ArticleSearchRequest request) {
		return articleRepository.searchForId(request);
	}

	public Page<Article> getArticles(ArticleSearchRequest request) {
		Pageable pageable = new PageRequest(0, 10);
		return getArticles(request, pageable);
	}

	public Page<Article> getArticles(ArticleSearchRequest request, Pageable pageable) {
		return articleRepository.search(request, pageable);
	}

	public List<Article> getArticles(Collection<Long> ids) {
		Set<Article> results = new LinkedHashSet<Article>(articleRepository.findAllByIdIn(ids));
		List<Article> articles = new ArrayList<>();
		for (long id : ids) {
			for (Article article : results) {
				if (id == article.getId()) {
					articles.add(article);
					break;
				}
			}
		}
		return articles;
	}

	@Cacheable(value = WallRideCacheConfiguration.ARTICLE_CACHE)
	public SortedSet<Article> getArticlesByCategoryCode(String language, String code, Post.Status status) {
		return getArticlesByCategoryCode(language, code, status, 10);
	}

	@Cacheable(value = WallRideCacheConfiguration.ARTICLE_CACHE)
	public SortedSet<Article> getArticlesByCategoryCode(String language, String code, Post.Status status, int size) {
		ArticleSearchRequest request = new ArticleSearchRequest()
				.withLanguage(language)
				.withCategoryCodes(code)
				.withStatus(status);

		Pageable pageable = new PageRequest(0, size);
		Page<Article> page = articleRepository.search(request, pageable);
		return new TreeSet<>(page.getContent());
	}

	@Cacheable(value = WallRideCacheConfiguration.ARTICLE_CACHE)
	public SortedSet<Article> getLatestArticles(String language, Post.Status status, int size) {
		ArticleSearchRequest request = new ArticleSearchRequest()
				.withLanguage(language)
				.withStatus(status);

		Pageable pageable = new PageRequest(0, size);
		Page<Article> page = articleRepository.search(request, pageable);
		return new TreeSet<>(page.getContent());
	}

	public Article getArticleById(long id) {
		return articleRepository.findOneById(id);
	}

	public Article getArticleById(long id, String language) {
		return articleRepository.findOneByIdAndLanguage(id, language);
	}

	public Article getArticleByCode(String code, String language) {
		return articleRepository.findOneByCodeAndLanguage(code, language);
	}

	public Article getDraftById(long id) {
		return articleRepository.findOne(ArticleSpecifications.draft(entityManager.getReference(Article.class, id)));
	}

	public long countArticles(String language) {
		return articleRepository.count(language);
	}

	public long countArticlesByStatus(Post.Status status, String language) {
		return articleRepository.countByStatus(status, language);
	}

	public Map<Long, Long> countArticlesByAuthorIdGrouped(Post.Status status, String language) {
		List<Map<String, Object>> results = articleRepository.countByAuthorIdGrouped(status, language);
		Map<Long, Long> counts = new HashMap<>();
		for (Map<String, Object> row : results) {
			counts.put((Long) row.get("userId"), (Long) row.get("count"));
		}
		return counts;
	}

	public Map<Long, Long> countArticlesByCategoryIdGrouped(Post.Status status, String language) {
		List<Map<String, Object>> results = articleRepository.countByCategoryIdGrouped(status, language);
		Map<Long, Long> counts = new HashMap<>();
		for (Map<String, Object> row : results) {
			counts.put((Long) row.get("categoryId"), (Long) row.get("count"));
		}
		return counts;
	}

	public Map<Long, Long> countArticlesByTagIdGrouped(Post.Status status, String language) {
		List<Map<String, Object>> results = articleRepository.countByTagIdGrouped(status, language);
		Map<Long, Long> counts = new HashMap<>();
		for (Map<String, Object> row : results) {
			counts.put((Long) row.get("tagId"), (Long) row.get("count"));
		}
		return counts;
	}
}
