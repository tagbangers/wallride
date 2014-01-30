package org.wallride.blog.service;

import org.springframework.context.annotation.Lazy;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.wallride.blog.web.page.PageSearchForm;
import org.wallride.core.domain.Page;
import org.wallride.core.domain.Post;
import org.wallride.core.repository.PageFullTextSearchTerm;
import org.wallride.core.repository.PageRepository;
import org.wallride.core.support.Paginator;

import javax.inject.Inject;
import java.util.*;

@Service @Lazy
@Transactional(rollbackFor=Exception.class)
public class PageService {

	@Inject
	private PageRepository pageRepository;

	public List<Long> searchPages(PageSearchForm form) {
		PageFullTextSearchTerm term = form.toFullTextSearchTerm();
		term.setStatus(Post.Status.PUBLISHED);
		term.setLanguage(LocaleContextHolder.getLocale().getLanguage());
		return pageRepository.findByFullTextSearchTerm(term);
	}

	public List<Page> readPages(Paginator<Long> paginator) {
		if (paginator == null || !paginator.hasElement()) return new ArrayList<Page>();
		return readPages(paginator.getElements());
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

	public Page readPage(String code) {
		return pageRepository.findByCode(code, LocaleContextHolder.getLocale().getLanguage());
	}
}
