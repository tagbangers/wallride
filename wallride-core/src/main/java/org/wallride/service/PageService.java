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
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MessageCodesResolver;
import org.wallride.autoconfigure.WallRideCacheConfiguration;
import org.wallride.autoconfigure.WallRideProperties;
import org.wallride.domain.CustomField;
import org.wallride.domain.CustomFieldValue;
import org.wallride.domain.*;
import org.wallride.exception.DuplicateCodeException;
import org.wallride.exception.EmptyCodeException;
import org.wallride.model.*;
import org.wallride.repository.*;
import org.wallride.support.AuthorizedUser;
import org.wallride.web.controller.admin.article.CustomFieldValueEditForm;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Transactional(rollbackFor = Exception.class)
public class PageService {

	@Resource
	private PostRepository postRepository;

	@Resource
	private PageRepository pageRepository;

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

	private static Logger logger = LoggerFactory.getLogger(PageService.class);

	@CacheEvict(value = WallRideCacheConfiguration.PAGE_CACHE, allEntries = true)
	public Page createPage(PageCreateRequest request, Post.Status status, AuthorizedUser authorizedUser) {
		LocalDateTime now = LocalDateTime.now();

		String code = (request.getCode() != null) ? request.getCode() : request.getTitle();
		if (!StringUtils.hasText(code)) {
			if (!status.equals(Post.Status.DRAFT)) {
				throw new EmptyCodeException();
			}
		}

		if (!status.equals(Post.Status.DRAFT)) {
			Post duplicate = postRepository.findOneByCodeAndLanguage(code, request.getLanguage());
			if (duplicate != null) {
				throw new DuplicateCodeException(request.getCode());
			}
		}

		Page page = new Page();

		if (!status.equals(Post.Status.DRAFT)) {
			page.setCode(code);
			page.setDraftedCode(null);
		} else {
			page.setCode(null);
			page.setDraftedCode(code);
		}

		Page parent = (request.getParentId() != null) ? pageRepository.findOneByIdAndLanguage(request.getParentId(), request.getLanguage()) : null;
		int rgt = 0;
		if (parent == null) {
			rgt = pageRepository.findMaxRgt();
			rgt++;
		} else {
			rgt = parent.getRgt();
			pageRepository.unshiftRgt(rgt);
			pageRepository.unshiftLft(rgt);
		}

		page.setParent(parent);

		Media cover = null;
		if (request.getCoverId() != null) {
			cover = entityManager.getReference(Media.class, request.getCoverId());
		}
		page.setCover(cover);
		page.setTitle(request.getTitle());
		page.setBody(request.getBody());

		page.setAuthor(entityManager.getReference(User.class, authorizedUser.getId()));

		LocalDateTime date = request.getDate();
		if (Post.Status.PUBLISHED.equals(status)) {
			if (date == null) {
				date = now;
			} else if (date.isAfter(now)) {
				status = Post.Status.SCHEDULED;
			}
		}
		page.setDate(date);
		page.setStatus(status);
		page.setLanguage(request.getLanguage());

		page.getCategories().clear();
		SortedSet<Category> categories = new TreeSet<>();
		for (long categoryId : request.getCategoryIds()) {
			categories.add(entityManager.getReference(Category.class, categoryId));
		}
		page.setCategories(categories);

		page.getTags().clear();
		Set<String> tagNames = StringUtils.commaDelimitedListToSet(request.getTags());
		if (!CollectionUtils.isEmpty(tagNames)) {
			for (String tagName : tagNames) {
				Tag tag = tagRepository.findOneForUpdateByNameAndLanguage(tagName, request.getLanguage());
				if (tag == null) {
					tag = new Tag();
					tag.setName(tagName);
					tag.setLanguage(request.getLanguage());
					page.setCreatedAt(now);
					page.setCreatedBy(authorizedUser.toString());
					page.setUpdatedAt(now);
					page.setUpdatedBy(authorizedUser.toString());
					tag = tagRepository.saveAndFlush(tag);
				}
				page.getTags().add(tag);
			}
		}

		page.getRelatedPosts().clear();
		Set<Post> relatedPosts = new HashSet<>();
		for (long relatedId : request.getRelatedPostIds()) {
			relatedPosts.add(entityManager.getReference(Post.class, relatedId));
		}
		page.setRelatedToPosts(relatedPosts);

		Seo seo = new Seo();
		seo.setTitle(request.getSeoTitle());
		seo.setDescription(request.getSeoDescription());
		seo.setKeywords(request.getSeoKeywords());
		page.setSeo(seo);

		page.setLft(rgt);
		page.setRgt(rgt + 1);

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
		page.setMedias(medias);

		page.setCreatedAt(now);
		page.setCreatedBy(authorizedUser.toString());
		page.setUpdatedAt(now);
		page.setUpdatedBy(authorizedUser.toString());

		page.getCustomFieldValues().clear();
		if (!CollectionUtils.isEmpty(request.getCustomFieldValues())) {
			for (CustomFieldValueEditForm valueForm : request.getCustomFieldValues()) {
				CustomFieldValue value =  new CustomFieldValue();
				value.setCustomField(entityManager.getReference(CustomField.class, valueForm.getCustomFieldId()));
				value.setPost(page);
				if (valueForm.getFieldType().equals(CustomField.FieldType.CHECKBOX)) {
					value.setTextValue(String.join(",", valueForm.getTextValues()));
				} else {
					value.setTextValue(valueForm.getTextValue());
				}
				value.setStringValue(valueForm.getStringValue());
				value.setNumberValue(valueForm.getNumberValue());
				value.setDateValue(valueForm.getDateValue());
				value.setDatetimeValue(valueForm.getDatetimeValue());
				page.getCustomFieldValues().add(value);
			}
		}

		return pageRepository.save(page);
	}

	@CacheEvict(value = WallRideCacheConfiguration.PAGE_CACHE, allEntries = true)
	public Page savePageAsDraft(PageUpdateRequest request, AuthorizedUser authorizedUser) {
		postRepository.lock(request.getId());
		Page page = pageRepository.findOneByIdAndLanguage(request.getId(), request.getLanguage());
		if (!page.getStatus().equals(Post.Status.DRAFT)) {
			Page draft = pageRepository.findOne(PageSpecifications.draft(page));
			if (draft == null) {
				PageCreateRequest createRequest = new PageCreateRequest.Builder()
						.code(request.getCode())
						.coverId(request.getCoverId())
						.title(request.getTitle())
						.body(request.getBody())
						.authorId(request.getAuthorId())
						.date(request.getDate())
						.parentId(request.getParentId())
						.categoryIds(request.getCategoryIds())
						.tags(request.getTags())
						.seoTitle(request.getSeoTitle())
						.seoDescription(request.getSeoDescription())
						.seoKeywords(request.getSeoKeywords())
						.customFieldValues(new LinkedHashSet<>(request.getCustomFieldValues()))
						.language(request.getLanguage())
						.build();
				draft = createPage(createRequest, Post.Status.DRAFT, authorizedUser);
				draft.setDrafted(page);
				return pageRepository.save(draft);
			} else {
				PageUpdateRequest updateRequest = new PageUpdateRequest.Builder()
						.id(draft.getId())
						.code(request.getCode())
						.coverId(request.getCoverId())
						.title(request.getTitle())
						.body(request.getBody())
						.authorId(request.getAuthorId())
						.date(request.getDate())
						.parentId(request.getParentId())
						.categoryIds(request.getCategoryIds())
						.tags(request.getTags())
						.seoTitle(request.getSeoTitle())
						.seoDescription(request.getSeoDescription())
						.seoKeywords(request.getSeoKeywords())
						.customFieldValues(request.getCustomFieldValues())
						.language(request.getLanguage())
						.build();
				return savePage(updateRequest, authorizedUser);
			}
		} else {
			return savePage(request, authorizedUser);
		}
	}

	@CacheEvict(value = WallRideCacheConfiguration.PAGE_CACHE, allEntries = true)
	public Page savePageAsPublished(PageUpdateRequest request, AuthorizedUser authorizedUser) {
		postRepository.lock(request.getId());
		Page page = pageRepository.findOneByIdAndLanguage(request.getId(), request.getLanguage());
		Page deleteTarget = getDraftById(page.getId());
		if (deleteTarget != null) {
			pageRepository.delete(deleteTarget);
		}
		page.setDrafted(null);
		page.setStatus(Post.Status.PUBLISHED);
		pageRepository.save(page);
		return savePage(request, authorizedUser);
	}

	@CacheEvict(value = WallRideCacheConfiguration.PAGE_CACHE, allEntries = true)
	public Page savePageAsUnpublished(PageUpdateRequest request, AuthorizedUser authorizedUser) {
		postRepository.lock(request.getId());
		Page page = pageRepository.findOneByIdAndLanguage(request.getId(), request.getLanguage());
		Page deleteTarget = getDraftById(page.getId());
		if (deleteTarget != null) {
			pageRepository.delete(deleteTarget);
		}
		page.setDrafted(null);
		page.setStatus(Post.Status.DRAFT);
		pageRepository.save(page);
		pageRepository.deleteByDrafted(page);
		return savePage(request, authorizedUser);
	}

	@CacheEvict(value = WallRideCacheConfiguration.PAGE_CACHE, allEntries = true)
	public Page savePage(PageUpdateRequest request, AuthorizedUser authorizedUser) {
		postRepository.lock(request.getId());
		Page page = pageRepository.findOneByIdAndLanguage(request.getId(), request.getLanguage());
		LocalDateTime now = LocalDateTime.now();

		String code = (request.getCode() != null) ? request.getCode() : request.getTitle();
		if (!StringUtils.hasText(code)) {
			if (!page.getStatus().equals(Post.Status.DRAFT)) {
				throw new EmptyCodeException();
			}
		}
		if (!page.getStatus().equals(Post.Status.DRAFT)) {
			Post duplicate = postRepository.findOneByCodeAndLanguage(request.getCode(), request.getLanguage());
			if (duplicate != null && !duplicate.equals(page)) {
				throw new DuplicateCodeException(request.getCode());
			}
		}

		if (!page.getStatus().equals(Post.Status.DRAFT)) {
			page.setCode(code);
			page.setDraftedCode(null);
		} else {
			page.setCode(null);
			page.setDraftedCode(code);
		}

		Page parent = (request.getParentId() != null) ? entityManager.getReference(Page.class, request.getParentId()) : null;
		if (!(page.getParent() == null && parent == null) && !ObjectUtils.nullSafeEquals(page.getParent(), parent)) {
			pageRepository.shiftLftRgt(page.getLft(), page.getRgt());
			pageRepository.shiftRgt(page.getRgt());
			pageRepository.shiftLft(page.getRgt());

			int rgt = 0;
			if (parent == null) {
				rgt = pageRepository.findMaxRgt();
				rgt++;
			} else {
				rgt = parent.getRgt();
				pageRepository.unshiftRgt(rgt);
				pageRepository.unshiftLft(rgt);
			}
			page.setLft(rgt);
			page.setRgt(rgt + 1);
		}

		page.setParent(parent);

		Media cover = null;
		if (request.getCoverId() != null) {
			cover = entityManager.getReference(Media.class, request.getCoverId());
		}
		page.setCover(cover);
		page.setTitle(request.getTitle());
		page.setBody(request.getBody());

//		User author = null;
//		if (request.getAuthorId() != null) {
//			author = entityManager.getReference(User.class, request.getAuthorId());
//		}
//		page.setAuthor(author);

		LocalDateTime date = request.getDate();
		if (Post.Status.PUBLISHED.equals(page.getStatus())) {
			if (date == null) {
				date = now.truncatedTo(ChronoUnit.HOURS);
			} else if (date.isAfter(now)) {
				page.setStatus(Post.Status.SCHEDULED);
			}
		}
		page.setDate(date);
		page.setLanguage(request.getLanguage());

		page.getCategories().clear();
		SortedSet<Category> categories = new TreeSet<>();
		for (long categoryId : request.getCategoryIds()) {
			categories.add(entityManager.getReference(Category.class, categoryId));
		}
		page.setCategories(categories);

		page.getTags().clear();
		Set<String> tagNames = StringUtils.commaDelimitedListToSet(request.getTags());
		if (!CollectionUtils.isEmpty(tagNames)) {
			for (String tagName : tagNames) {
				Tag tag = tagRepository.findOneForUpdateByNameAndLanguage(tagName, request.getLanguage());
				if (tag == null) {
					tag = new Tag();
					tag.setName(tagName);
					tag.setLanguage(request.getLanguage());
					page.setCreatedAt(now);
					page.setCreatedBy(authorizedUser.toString());
					page.setUpdatedAt(now);
					page.setUpdatedBy(authorizedUser.toString());
					tag = tagRepository.saveAndFlush(tag);
				}
				page.getTags().add(tag);
			}
		}

		page.getRelatedPosts().clear();
		Set<Post> relatedPosts = new HashSet<>();
		for (long relatedId : request.getRelatedPostIds()) {
			relatedPosts.add(entityManager.getReference(Post.class, relatedId));
		}
		page.setRelatedToPosts(relatedPosts);

		Seo seo = new Seo();
		seo.setTitle(request.getSeoTitle());
		seo.setDescription(request.getSeoDescription());
		seo.setKeywords(request.getSeoKeywords());
		page.setSeo(seo);

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
		page.setMedias(medias);

		page.setUpdatedAt(now);
		page.setUpdatedBy(authorizedUser.toString());

		SortedSet<CustomFieldValue> fieldValues = new TreeSet<>();
		Map<CustomField, CustomFieldValue> valueMap = new LinkedHashMap<>();
		for (CustomFieldValue value : page.getCustomFieldValues()) {
			valueMap.put(value.getCustomField(), value);
		}

		page.getCustomFieldValues().clear();
		if (!CollectionUtils.isEmpty(request.getCustomFieldValues())) {
			for (CustomFieldValueEditForm valueForm : request.getCustomFieldValues()) {
				CustomField customField = entityManager.getReference(CustomField.class, valueForm.getCustomFieldId());
				CustomFieldValue value = valueMap.get(customField);
				if (value == null) {
					value = new CustomFieldValue();
				}
				value.setCustomField(customField);
				value.setPost(page);
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
		page.setCustomFieldValues(fieldValues);

		return pageRepository.save(page);
	}

	@CacheEvict(value = WallRideCacheConfiguration.PAGE_CACHE, allEntries = true)
	public void updatePageHierarchy(List<Map<String, Object>> data, String language) {
		for (int i = 0; i < data.size(); i++) {
			Map<String, Object> map = data.get(i);
			if (map.get("item_id") != null) {
				postRepository.lock(Long.parseLong((String) map.get("item_id")));
				Page page = pageRepository.findOneByIdAndLanguage(Long.parseLong((String) map.get("item_id")), language);
				if (page != null) {
					Page parent = null;
					if (map.get("parent_id") != null) {
						parent = pageRepository.findOneByIdAndLanguage(Long.parseLong((String) map.get("parent_id")), language);
					}
					page.setParent(parent);
					page.setLft(((int) map.get("left")) - 1);
					page.setRgt(((int) map.get("right")) - 1);
//					page.setDepth((int) map.get("depth"));
//					page.setSort(i);
					pageRepository.save(page);
				}
			}
		}
	}

	@CacheEvict(value = WallRideCacheConfiguration.PAGE_CACHE, allEntries = true)
	public Page deletePage(PageDeleteRequest request, BindingResult result) throws BindException {
		postRepository.lock(request.getId());
		Page page = pageRepository.findOneByIdAndLanguage(request.getId(), request.getLanguage());
		Page parent = page.getParent();
		for (Page child : page.getChildren()) {
			child.setParent(parent);
			pageRepository.saveAndFlush(child);
		}
		page.getChildren().clear();
		pageRepository.saveAndFlush(page);
		pageRepository.delete(page);

		pageRepository.shiftLftRgt(page.getLft(), page.getRgt());
		pageRepository.shiftRgt(page.getRgt());
		pageRepository.shiftLft(page.getRgt());

		return page;
	}

	@CacheEvict(value = WallRideCacheConfiguration.PAGE_CACHE, allEntries = true)
	public Page deletePage(long id, String language) {
		postRepository.lock(id);
		Page page = pageRepository.findOneByIdAndLanguage(id, language);
		Page parent = page.getParent();
		for (Page child : page.getChildren()) {
			child.setParent(parent);
			pageRepository.saveAndFlush(child);
		}
		page.getChildren().clear();
		pageRepository.saveAndFlush(page);
		pageRepository.delete(page);

		pageRepository.shiftLftRgt(page.getLft(), page.getRgt());
		pageRepository.shiftRgt(page.getRgt());
		pageRepository.shiftLft(page.getRgt());

		return page;
	}

	@CacheEvict(value = WallRideCacheConfiguration.PAGE_CACHE, allEntries = true)
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public List<Page> bulkDeletePage(PageBulkDeleteRequest bulkDeleteRequest, BindingResult result) {
		List<Page> pages = new ArrayList<>();
		for (long id : bulkDeleteRequest.getIds()) {
			final PageDeleteRequest deleteRequest = new PageDeleteRequest.Builder()
					.id(id)
					.language(bulkDeleteRequest.getLanguage())
					.build();

			final BeanPropertyBindingResult r = new BeanPropertyBindingResult(deleteRequest, "request");
			r.setMessageCodesResolver(messageCodesResolver);

			TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
			transactionTemplate.setPropagationBehavior(TransactionTemplate.PROPAGATION_REQUIRES_NEW);
			Page page = null;
			try {
				page = transactionTemplate.execute(new TransactionCallback<Page>() {
					public Page doInTransaction(TransactionStatus status) {
						try {
							return deletePage(deleteRequest, r);
						} catch (BindException e) {
							throw new RuntimeException(e);
						}
					}
				});
				pages.add(page);
			} catch (Exception e) {
				logger.debug("Errors: {}", r);
				result.addAllErrors(r);
			}
		}
		return pages;
	}

	public List<Long> getPageIds(PageSearchRequest request) {
		return pageRepository.searchForId(request);
	}

	@Cacheable(value = WallRideCacheConfiguration.PAGE_CACHE)
	public org.springframework.data.domain.Page<Page> getPages(PageSearchRequest request) {
		return getPages(request, null);
	}

	@Cacheable(value = WallRideCacheConfiguration.PAGE_CACHE)
	public org.springframework.data.domain.Page<Page> getPages(PageSearchRequest request, Pageable pageable) {
		return pageRepository.search(request, pageable);
	}

	@Cacheable(value = WallRideCacheConfiguration.PAGE_CACHE)
	public List<Page> getPathPages(Page page) {
		return getPathPages(page, false);
	}

	@Cacheable(value = WallRideCacheConfiguration.PAGE_CACHE)
	public List<Page> getPathPages(Page page, boolean includeUnpublished) {
		return pageRepository.findAll(PageSpecifications.path(page, includeUnpublished));
	}

	public List<Page> getChildPages(Page page) {
		return getChildPages(page, false);
	}

	public List<Page> getChildPages(Page page, boolean includeUnpublished) {
		return pageRepository.findAll(PageSpecifications.children(page, includeUnpublished));
	}

	public List<Page> getSiblingPages(Page page) {
		return getSiblingPages(page, false);
	}

	public List<Page> getSiblingPages(Page page, boolean includeUnpublished) {
		return pageRepository.findAll(PageSpecifications.siblings(page, includeUnpublished));
	}

	public Page getPageById(long id, String language) {
		return pageRepository.findOneByIdAndLanguage(id, language);
	}

	public Page getPageByCode(String code, String language) {
		return pageRepository.findOneByCodeAndLanguage(code, language);
	}

	public Page getDraftById(long id) {
		return pageRepository.findOne(PageSpecifications.draft(entityManager.getReference(Page.class, id)));
	}

	public long countPages(String language) {
		return pageRepository.count(language);
	}

	public long countPagesByStatus(Post.Status status, String language) {
		return pageRepository.countByStatus(status, language);
	}
}
