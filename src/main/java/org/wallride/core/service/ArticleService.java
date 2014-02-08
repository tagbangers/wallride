package org.wallride.core.service;

import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
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
import org.wallride.core.support.Paginator;
import org.wallride.core.support.Settings;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service @Lazy
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
	public Article createArticle(ArticleCreateRequest form, BindingResult errors, Post.Status status, AuthorizedUser authorizedUser) throws BindException {
		LocalDateTime now = new LocalDateTime();

		String code = (form.getCode() != null) ? form.getCode() : form.getTitle();
		if (!StringUtils.hasText(code)) {
			if (Post.Status.PUBLISHED.equals(status)) {
				errors.rejectValue("code", "NotNull");
			}
		}
		else {
			Article duplicate = articleRepository.findByCode(form.getCode(), form.getLanguage());
			if (duplicate != null) {
				errors.rejectValue("code", "NotDuplicate");
			}
		}

		if (errors.hasErrors()) {
			throw new BindException(errors);
		}

		Article article = new Article();
		Media cover = null;
		if (form.getCoverId() != null) {
			cover = entityManager.getReference(Media.class, form.getCoverId());
		}
		article.setCover(cover);
		article.setTitle(form.getTitle());
		article.setCode(code);
		article.setBody(form.getBody());

		User author = entityManager.getReference(User.class, authorizedUser.getId());
//		if (request.getAuthorId() != null) {
//			author = entityManager.getReference(User.class, request.getAuthorId());
//		}
		article.setAuthor(author);

		LocalDateTime date = form.getDate();
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
		article.setLanguage(form.getLanguage());

		article.getCategories().clear();
		for (long categoryId : form.getCategoryIds()) {
			article.getCategories().add(entityManager.getReference(Category.class, categoryId));
		}

		List<Media> medias = new ArrayList<>();
		if (StringUtils.hasText(form.getBody())) {
			String mediaUrlPrefix = settings.readSettingAsString(Setting.Key.MEDIA_URL_PREFIX);
			Pattern mediaUrlPattern = Pattern.compile(String.format("%s([0-9a-zA-Z\\-]+)", mediaUrlPrefix));
			Matcher mediaUrlMatcher = mediaUrlPattern.matcher(form.getBody());
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

	@CacheEvict(value="articles", allEntries=true)
	public Article updateArticle(ArticleUpdateRequest form, BindingResult errors, Post.Status status, AuthorizedUser authorizedUser) throws BindException {
		LocalDateTime now = new LocalDateTime();
		Article article = articleRepository.findByIdForUpdate(form.getId(), form.getLanguage());

		String code = (form.getCode() != null) ? form.getCode() : form.getTitle();
		if (!StringUtils.hasText(code)) {
			if (Post.Status.PUBLISHED.equals(status)) {
				errors.rejectValue("code", "NotNull");
			}
		}
		else {
			Article duplicate = articleRepository.findByCode(form.getCode(), form.getLanguage());
			if (duplicate != null && !duplicate.equals(article)) {
				errors.rejectValue("code", "NotDuplicate");
			}
		}

		if (errors.hasErrors()) {
			throw new BindException(errors);
		}

		Media cover = null;
		if (form.getCoverId() != null) {
			cover = entityManager.getReference(Media.class, form.getCoverId());
		}
		article.setCover(cover);
		article.setTitle(form.getTitle());
		article.setCode(code);
		article.setBody(form.getBody());

//		User author = null;
//		if (request.getAuthorId() != null) {
//			author = entityManager.getReference(User.class, request.getAuthorId());
//		}
//		article.setAuthor(author);

		LocalDateTime date = form.getDate();
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
		article.setLanguage(form.getLanguage());

		article.getCategories().clear();
		for (long categoryId : form.getCategoryIds()) {
			article.getCategories().add(entityManager.getReference(Category.class, categoryId));
		}

		List<Media> medias = new ArrayList<>();
		if (StringUtils.hasText(form.getBody())) {
			String mediaUrlPrefix = settings.readSettingAsString(Setting.Key.MEDIA_URL_PREFIX);
			Pattern mediaUrlPattern = Pattern.compile(String.format("%s([0-9a-zA-Z\\-]+)", mediaUrlPrefix));
			Matcher mediaUrlMatcher = mediaUrlPattern.matcher(form.getBody());
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
	
	public List<Long> searchArticles(ArticleSearchRequest request) {
		if (request.isEmpty()) {
			return articleRepository.findId();
		}
		ArticleFullTextSearchTerm term = request.toFullTextSearchTerm();
		term.setLanguage(LocaleContextHolder.getLocale().getLanguage());
		return articleRepository.findByFullTextSearchTerm(request.toFullTextSearchTerm());
	}

	public List<Article> readArticles(Paginator<Long> paginator) {
		if (paginator == null || !paginator.hasElement()) return new ArrayList<Article>();
		return readArticles(paginator.getElements());
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

	public Article readArticle(String code, String language) {
		return articleRepository.findByCode(code, language);
	}

	public long countArticles(String language) {
		return articleRepository.count(language);
	}

	public long countArticlesByStatus(Post.Status status, String language) {
		return articleRepository.countByStatus(status, language);
	}
}
