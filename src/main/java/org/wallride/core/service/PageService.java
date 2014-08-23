package org.wallride.core.service;

import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MessageCodesResolver;
import org.wallride.core.domain.*;
import org.wallride.core.repository.MediaRepository;
import org.wallride.core.repository.PageFullTextSearchTerm;
import org.wallride.core.repository.PageRepository;
import org.wallride.core.support.AuthorizedUser;
import org.wallride.core.support.Settings;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Transactional(rollbackFor=Exception.class)
public class PageService {
	
	@Resource
	private PageRepository pageRepository;
	@Resource
	private MediaRepository mediaRepository;

	@Inject
	private MessageCodesResolver messageCodesResolver;
	@Inject
	private PlatformTransactionManager transactionManager;
	@Inject
	private Settings settings;

	@PersistenceContext
	private EntityManager entityManager;

	private static Logger logger = LoggerFactory.getLogger(PageService.class);

	@CacheEvict(value = "pages", allEntries = true)
	public Page createPage(PageCreateRequest request, Post.Status status, AuthorizedUser authorizedUser) {
		LocalDateTime now = new LocalDateTime();

		String code = (request.getCode() != null) ? request.getCode() : request.getTitle();
		if (!StringUtils.hasText(code)) {
			if (!status.equals(Post.Status.DRAFT)) {
				throw new EmptyCodeException();
			}
		}
		
		if (!status.equals(Post.Status.DRAFT)) {
			Page duplicate = pageRepository.findByCode(request.getCode(), request.getLanguage());
			if (duplicate != null) {
				throw new DuplicateCodeException(request.getCode());
			}
		}

		Page page = new Page();

		if (!status.equals(Post.Status.DRAFT)) {
			page.setCode(code);
			page.setDraftedCode(null);
		}
		else {
			page.setCode(null);
			page.setDraftedCode(code);
		}
		
		Page parent = (request.getParentId() != null) ? pageRepository.findById(request.getParentId(), request.getLanguage()) : null;
		int rgt = 0;
		if (parent == null) {
			rgt = pageRepository.findMaxRgt();
			rgt++;
		}
		else {
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
				date = now.withTime(0, 0, 0, 0);
			}
			else if (date.isAfter(now)) {
				status = Post.Status.SCHEDULED;
			}
		}
		page.setDate(date);
		page.setStatus(status);
		page.setLanguage(request.getLanguage());

		page.getRelatedPosts().clear();
		Set<Post> relatedPosts = new HashSet<>();
		for (long relatedId : request.getRelatedPostIds()) {
			relatedPosts.add(entityManager.getReference(Post.class, relatedId));
		}
		page.setRelatedPosts(relatedPosts);

		Seo seo = new Seo();
		seo.setTitle(request.getSeoTitle());
		seo.setDescription(request.getSeoDescription());
		seo.setKeywords(request.getSeoKeywords());
		page.setSeo(seo);

		page.setLft(rgt);
		page.setRgt(rgt + 1);

		List<Media> medias = new ArrayList<>();
		if (StringUtils.hasText(request.getBody())) {
			String mediaUrlPrefix = settings.readSettingAsString(Setting.Key.MEDIA_URL_PREFIX);
			Pattern mediaUrlPattern = Pattern.compile(String.format("%s([0-9a-zA-Z\\-]+)", mediaUrlPrefix));
			Matcher mediaUrlMatcher = mediaUrlPattern.matcher(request.getBody());
			while (mediaUrlMatcher.find()) {
				Media media = mediaRepository.findById(mediaUrlMatcher.group(1));
				medias.add(media);
			}
		}
		page.setMedias(medias);

		page.setCreatedAt(now);
		page.setCreatedBy(authorizedUser.toString());
		page.setUpdatedAt(now);
		page.setUpdatedBy(authorizedUser.toString());

		return pageRepository.save(page);
	}

	@CacheEvict(value = "pages", allEntries = true)
	public Page savePageAsDraft(PageUpdateRequest request, AuthorizedUser authorizedUser) {
		Page page = pageRepository.findByIdForUpdate(request.getId(), request.getLanguage());
		if (!page.getStatus().equals(Post.Status.DRAFT)) {
			Page draft = pageRepository.findDraft(page);
			if (draft == null) {
				PageCreateRequest createRequest = new PageCreateRequest.Builder()
						.code(request.getCode())
						.coverId(request.getCoverId())
						.title(request.getTitle())
						.body(request.getBody())
						.authorId(request.getAuthorId())
						.date(request.getDate())
						.parentId(request.getParentId())
						.language(request.getLanguage())
						.build();
				draft = createPage(createRequest, Post.Status.DRAFT, authorizedUser);
				draft.setDrafted(page);
				return pageRepository.save(draft);
			}
			else {
				PageUpdateRequest updateRequest = new PageUpdateRequest.Builder()
						.id(draft.getId())
						.code(request.getCode())
						.coverId(request.getCoverId())
						.title(request.getTitle())
						.body(request.getBody())
						.authorId(request.getAuthorId())
						.date(request.getDate())
						.parentId(request.getParentId())
						.language(request.getLanguage())
						.build();
				return savePage(updateRequest, authorizedUser);
			}
		}
		else {
			return savePage(request, authorizedUser);
		}
	}

	@CacheEvict(value = "pages", allEntries = true)
	public Page savePageAsPublished(PageUpdateRequest request, AuthorizedUser authorizedUser) {
		Page page = pageRepository.findByIdForUpdate(request.getId(), request.getLanguage());
		page.setDrafted(null);
		page.setStatus(Post.Status.PUBLISHED);
		pageRepository.save(page);
		pageRepository.deleteByDrafted(page);
		return savePage(request, authorizedUser);
	}

	@CacheEvict(value = "pages", allEntries = true)
	public Page savePageAsUnpublished(PageUpdateRequest request, AuthorizedUser authorizedUser) {
		Page page = pageRepository.findByIdForUpdate(request.getId(), request.getLanguage());
		page.setDrafted(null);
		page.setStatus(Post.Status.DRAFT);
		pageRepository.save(page);
		pageRepository.deleteByDrafted(page);
		return savePage(request, authorizedUser);
	}

	@CacheEvict(value = "pages", allEntries = true)
	public Page savePage(PageUpdateRequest request, AuthorizedUser authorizedUser) {
		Page page = pageRepository.findByIdForUpdate(request.getId(), request.getLanguage());
		LocalDateTime now = new LocalDateTime();

		String code = (request.getCode() != null) ? request.getCode() : request.getTitle();
		if (!StringUtils.hasText(code)) {
			if (!page.getStatus().equals(Post.Status.DRAFT)) {
				throw new EmptyCodeException();
			}
		}
		if (!page.getStatus().equals(Post.Status.DRAFT)) {
			Page duplicate = pageRepository.findByCode(request.getCode(), request.getLanguage());
			if (duplicate != null && !duplicate.equals(page)) {
				throw new DuplicateCodeException(request.getCode());
			}
		}

		if (!page.getStatus().equals(Post.Status.DRAFT)) {
			page.setCode(code);
			page.setDraftedCode(null);
		}
		else {
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
			}
			else {
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
				date = now.withTime(0, 0, 0, 0);
			}
			else if (date.isAfter(now)) {
				page.setStatus(Post.Status.SCHEDULED);
			}
		}
		page.setDate(date);
		page.setLanguage(request.getLanguage());

		page.getRelatedPosts().clear();
		Set<Post> relatedPosts = new HashSet<>();
		for (long relatedId : request.getRelatedPostIds()) {
			relatedPosts.add(entityManager.getReference(Post.class, relatedId));
		}
		page.setRelatedPosts(relatedPosts);

		Seo seo = new Seo();
		seo.setTitle(request.getSeoTitle());
		seo.setDescription(request.getSeoDescription());
		seo.setKeywords(request.getSeoKeywords());
		page.setSeo(seo);

		List<Media> medias = new ArrayList<>();
		if (StringUtils.hasText(request.getBody())) {
			String mediaUrlPrefix = settings.readSettingAsString(Setting.Key.MEDIA_URL_PREFIX);
			Pattern mediaUrlPattern = Pattern.compile(String.format("%s([0-9a-zA-Z\\-]+)", mediaUrlPrefix));
			Matcher mediaUrlMatcher = mediaUrlPattern.matcher(request.getBody());
			while (mediaUrlMatcher.find()) {
				Media media = mediaRepository.findById(mediaUrlMatcher.group(1));
				medias.add(media);
			}
		}
		page.setMedias(medias);

		page.setUpdatedAt(now);
		page.setUpdatedBy(authorizedUser.toString());

		return pageRepository.save(page);
	}

	@CacheEvict(value = "pages", allEntries = true)
	public void updatePageHierarchy(List<Map<String, Object>> data, String language) {
		for (int i = 0; i < data.size(); i++) {
			Map<String, Object> map = data.get(i);
			if (map.get("item_id") != null) {
				Page page = pageRepository.findByIdForUpdate(Long.parseLong((String) map.get("item_id")), language);
				if (page != null) {
					Page parent = null;
					if (map.get("parent_id") != null) {
						parent = pageRepository.findById(Long.parseLong((String) map.get("parent_id")), language);
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

	@CacheEvict(value = "pages", allEntries = true)
	public Page deletePage(PageDeleteRequest request, BindingResult result) throws BindException {
		Page page = pageRepository.findByIdForUpdate(request.getId(), request.getLanguage());
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

	@CacheEvict(value = "pages", allEntries = true)
	public Page deletePage(long id, String language) {
		Page page = pageRepository.findByIdForUpdate(id, language);
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

	@CacheEvict(value = "pages", allEntries = true)
	@Transactional(propagation=Propagation.NOT_SUPPORTED)
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
			}
			catch (Exception e) {
				logger.debug("Errors: {}", r);
				result.addAllErrors(r);
			}
		}
		return pages;
	}

	public org.springframework.data.domain.Page<Page> readPages(PageSearchRequest request) {
		Pageable pageable = new PageRequest(0, 10);
		return readPages(request, pageable);
	}

	public org.springframework.data.domain.Page<Page> readPages(PageSearchRequest request, Pageable pageable) {
		PageFullTextSearchTerm term = request.toFullTextSearchTerm();
		term.setLanguage(LocaleContextHolder.getLocale().getLanguage());
		return pageRepository.findByFullTextSearchTerm(request.toFullTextSearchTerm(), pageable);
	}
	
	public List<Page> readPages(Collection<Long> ids) {
		Set<Page> results = new LinkedHashSet<Page>(pageRepository.findByIdIn(ids));
		List<Page> pages = new ArrayList<>();
		for (long id : ids) {
			for (Page page : results) {
				if (id == page.getId()) {
					pages.add(page);
					break;
				}
			}
		}
		return pages;
	}
	
	public Page readPageById(long id, String language) {
		return pageRepository.findById(id, language);
	}

	public Page readPageByCode(String code, String language) {
		return pageRepository.findByCode(code, language);
	}

	public Page readDraftById(long id) {
		return pageRepository.findDraft(entityManager.getReference(Page.class, id));
	}

	@Cacheable(value = "pages", key = "'tree.' + #language")
	public PageTree readPageTree(String language) {
		List<Page> pages = pageRepository.findByLanguage(language);
		return new PageTree(pages);
	}

	@Cacheable(value = "pages", key = "'tree.' + #language + '.' + #status")
	public PageTree readPageTree(String language, Post.Status status) {
		List<Page> pages = pageRepository.findByLanguageAndStatus(language, status);
		return new PageTree(pages);
	}

	public long countPages(String language) {
		return pageRepository.count(language);
	}

	public long countPagesByStatus(Post.Status status, String language) {
		return pageRepository.countByStatus(status, language);
	}
}
