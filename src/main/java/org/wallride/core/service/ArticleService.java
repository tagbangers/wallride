package org.wallride.core.service;

import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
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
import org.wallride.core.domain.*;
import org.wallride.core.repository.ArticleFullTextSearchTerm;
import org.wallride.core.repository.ArticleRepository;
import org.wallride.core.repository.MediaRepository;
import org.wallride.core.support.AuthorizedUser;
import org.wallride.core.support.Settings;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.lang.invoke.CallSite;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Transactional(rollbackFor=Exception.class)
public class ArticleService {
	
	@Inject
	private ArticleRepository articleRepository;

	@Inject
	private MediaRepository mediaRepository;
	
	@Inject
	private MessageCodesResolver messageCodesResolver;
	
	@Inject
	private PlatformTransactionManager transactionManager;

	@Inject
	private Settings settings;

	@PersistenceContext
	private EntityManager entityManager;

	private static Logger logger = LoggerFactory.getLogger(ArticleService.class); 

	@CacheEvict(value="articles", allEntries=true)
	public Article createArticle(ArticleCreateRequest request, Post.Status status, AuthorizedUser authorizedUser) {
		LocalDateTime now = new LocalDateTime();

		String code = (request.getCode() != null) ? request.getCode() : request.getTitle();
		if (!StringUtils.hasText(code)) {
			if (!status.equals(Post.Status.DRAFT)) {
				throw new EmptyCodeException();
			}
		}

		if (!status.equals(Post.Status.DRAFT)) {
			Article duplicate = articleRepository.findByCode(request.getCode(), request.getLanguage());
			if (duplicate != null) {
				throw new DuplicateCodeException(request.getCode());
			}
		}

		Article article = new Article();
		Media cover = null;
		if (request.getCoverId() != null) {
			cover = entityManager.getReference(Media.class, request.getCoverId());
		}
		article.setCover(cover);
		article.setTitle(request.getTitle());
		article.setCode(code);
		article.setBody(request.getBody());

		article.setAuthor(entityManager.getReference(User.class, authorizedUser.getId()));

		LocalDateTime date = request.getDate();
		if (Post.Status.PUBLISHED.equals(status)) {
			if (date == null) {
				date = now.withTime(0, 0, 0, 0);
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
		article.setMedias(medias);

		article.setCreatedAt(now);
		article.setCreatedBy(authorizedUser.toString());
		article.setUpdatedAt(now);
		article.setUpdatedBy(authorizedUser.toString());

		return articleRepository.save(article);
	}

	@CacheEvict(value = "articles", allEntries = true)
	public Article saveArticleAsDraft(ArticleUpdateRequest request, AuthorizedUser authorizedUser) {
		Article article = articleRepository.findByIdForUpdate(request.getId(), request.getLanguage());
		if (!article.getStatus().equals(Post.Status.DRAFT)) {
			List<Article> drafts = articleRepository.findByDrafted(article);
			if (CollectionUtils.isEmpty(drafts)) {
				ArticleCreateRequest createRequest = new ArticleCreateRequest.Builder()
						.code(request.getCode())
						.coverId(request.getCoverId())
						.title(request.getTitle())
						.body(request.getBody())
						.authorId(request.getAuthorId())
						.date(request.getDate())
						.categoryIds(request.getCategoryIds())
						.tagIds(request.getTagIds())
						.language(request.getLanguage())
						.build();
				Article draft = createArticle(createRequest, Post.Status.DRAFT, authorizedUser);
				draft.setDrafted(article);
				return articleRepository.save(draft);
			}
			else {
				Article draft = drafts.get(0);
				ArticleUpdateRequest updateRequest = new ArticleUpdateRequest.Builder()
						.id(draft.getId())
						.code(request.getCode())
						.coverId(request.getCoverId())
						.title(request.getTitle())
						.body(request.getBody())
						.authorId(request.getAuthorId())
						.date(request.getDate())
						.categoryIds(request.getCategoryIds())
						.tagIds(request.getTagIds())
						.language(request.getLanguage())
						.build();
				return saveArticle(updateRequest, authorizedUser);
			}
		}
		else {
			return saveArticle(request, authorizedUser);
		}
	}

	@CacheEvict(value = "articles", allEntries = true)
	public Article saveArticleAsPublished(ArticleUpdateRequest request, AuthorizedUser authorizedUser) {
		Article article = articleRepository.findByIdForUpdate(request.getId(), request.getLanguage());
		if (article.getStatus().equals(Post.Status.DRAFT)) {
			if (article.getDrafted() != null) {
				articleRepository.delete((Article) article.getDrafted());
			}
			article.setDrafted(null);
			article.getDrafts().clear();
			article.setStatus(Post.Status.PUBLISHED);
			articleRepository.save(article);
		}
		return saveArticle(request, authorizedUser);
	}

	@CacheEvict(value = "articles", allEntries = true)
	public Article saveArticleAsUnpublished(ArticleUpdateRequest request, AuthorizedUser authorizedUser) {
		Article article = articleRepository.findByIdForUpdate(request.getId(), request.getLanguage());
		if (!article.getStatus().equals(Post.Status.DRAFT)) {
			article.setDrafted(null);
			article.getDrafts().clear();
			article.setStatus(Post.Status.DRAFT);
			articleRepository.save(article);
		}
		return saveArticle(request, authorizedUser);
	}

//	@CacheEvict(value = "articles", allEntries = true)
//	public Article saveArticle(ArticleUpdateRequest request, AuthorizedUser authorizedUser) {
//		Article article = articleRepository.findByIdForUpdate(request.getId(), request.getLanguage());
//		return updateArticle(request, authorizedUser, article);
//	}

	@CacheEvict(value = "articles", allEntries = true)
	public Article saveArticle(ArticleUpdateRequest request, AuthorizedUser authorizedUser) {
		Article article = articleRepository.findByIdForUpdate(request.getId(), request.getLanguage());
		LocalDateTime now = new LocalDateTime();

		String code = (request.getCode() != null) ? request.getCode() : request.getTitle();
		if (!StringUtils.hasText(code)) {
			if (!Post.Status.DRAFT.equals(article.getStatus())) {
				throw new EmptyCodeException();
			}
		}
		Article duplicate = articleRepository.findByCode(request.getCode(), request.getLanguage());
		if (duplicate != null && !duplicate.equals(article)) {
			throw new DuplicateCodeException(request.getCode());
		}

		Media cover = null;
		if (request.getCoverId() != null) {
			cover = entityManager.getReference(Media.class, request.getCoverId());
		}
		article.setCover(cover);
		article.setTitle(request.getTitle());
		article.setCode(code);
		article.setBody(request.getBody());

//		User author = null;
//		if (request.getAuthorId() != null) {
//			author = entityManager.getReference(User.class, request.getAuthorId());
//		}
//		article.setAuthor(author);

		LocalDateTime date = request.getDate();
		if (Post.Status.PUBLISHED.equals(article.getStatus())) {
			if (date == null) {
				date = now.withTime(0, 0, 0, 0);
			}
			else if (date.isAfter(now)) {
				article.setStatus(Post.Status.SCHEDULED);
			}
		}
		article.setDate(date);
		article.setLanguage(request.getLanguage());

		article.getCategories().clear();
		for (long categoryId : request.getCategoryIds()) {
			article.getCategories().add(entityManager.getReference(Category.class, categoryId));
		}

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
		article.setMedias(medias);

		article.setUpdatedAt(now);
		article.setUpdatedBy(authorizedUser.toString());

		return articleRepository.save(article);
	}

	@CacheEvict(value="articles", allEntries=true)
	public Article deleteArticle(ArticleDeleteRequest request, BindingResult result) throws BindException {
		Article article = articleRepository.findByIdForUpdate(request.getId(), request.getLanguage());
		articleRepository.delete(article);
		return article;
	}
	
	@Transactional(propagation=Propagation.NOT_SUPPORTED)
	@CacheEvict(value="articles", allEntries=true)
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

	public Page<Article> readArticles(ArticleSearchRequest request) {
		Pageable pageable = new PageRequest(0, 10);
		return readArticles(request, pageable);
	}

	public Page<Article> readArticles(ArticleSearchRequest request, Pageable pageable) {
		ArticleFullTextSearchTerm term = request.toFullTextSearchTerm();
		term.setLanguage(LocaleContextHolder.getLocale().getLanguage());
		return articleRepository.findByFullTextSearchTerm(request.toFullTextSearchTerm(), pageable);
	}

	public List<Article> readArticles(Collection<Long> ids) {
		Set<Article> results = new LinkedHashSet<Article>(articleRepository.findByIdIn(ids));
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

	public Article readArticleById(long id, String language) {
		return articleRepository.findById(id, language);
	}

	public Article readArticleByCode(String code, String language) {
		return articleRepository.findByCode(code, language);
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
}
