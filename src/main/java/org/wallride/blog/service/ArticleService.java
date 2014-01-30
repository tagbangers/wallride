package org.wallride.blog.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.wallride.blog.web.article.ArticleSearchForm;
import org.wallride.core.domain.Article;
import org.wallride.core.domain.Post;
import org.wallride.core.repository.ArticleFullTextSearchTerm;
import org.wallride.core.repository.ArticleRepository;
import org.wallride.core.support.Paginator;

import javax.inject.Inject;
import java.util.*;

@Service @Lazy
@Transactional(rollbackFor=Exception.class)
public class ArticleService {

	@Inject
	private ArticleRepository articleRepository;

	@Cacheable("articles")
	public List<Long> searchArticles(ArticleSearchForm form) {
		ArticleFullTextSearchTerm term = form.toFullTextSearchTerm();
		term.setStatus(Post.Status.PUBLISHED);
		return articleRepository.findByFullTextSearchTerm(term);
	}

//	public Paginator<Long> searchArticles(int year, String language) {
//		ArticleFullTextSearchTerm term = new ArticleFullTextSearchTerm();
//		term.setStatus(Post.Status.PUBLISHED);
//		term.setDateFrom(new LocalDateTime(year, 1, 1, 0, 0, 0));
//		term.setDateTo(new LocalDateTime(year, 12, 31, 0, 0, 0));
//		term.setLanguage(language);
//		List<Long> ids = articleRepository.findByFullTextSearchTerm(term);
//		return new Paginator<Long>(ids, 20);
//	}
//
//	public Paginator<Long> searchArticles(int year, int month, String language) {
//		return null;
//	}
//
//	public Paginator<Long> searchArticles(int year, int month, int day, String language) {
//		return null;
//	}

	@Cacheable("articles")
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

	public Article readArticle(String code, String language) {
		return articleRepository.findByCode(code, language);
	}
}
