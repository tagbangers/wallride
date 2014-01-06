package org.wallride.core.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.wallride.core.domain.Category;
import org.wallride.core.domain.CategoryTree;
import org.wallride.core.repository.CategoryRepository;

import javax.inject.Inject;
import java.util.List;

@Service
@Transactional(rollbackFor=Exception.class)
public class CategoryTreeService {

	public static final String CATEGORY_CACHE_KEY = "categories";

	@Inject
	private CategoryRepository categoryRepository;

	@Cacheable(value=CATEGORY_CACHE_KEY, key="#language")
	public CategoryTree readCategoryTree(String language) {
		return readCategoryTree(language, false);
	}

	@Cacheable(value=CATEGORY_CACHE_KEY, key="#language")
	public CategoryTree readCategoryTree(String language, boolean hasArticle) {
		List<Category> categories = null;
		if (!hasArticle) {
			categories = categoryRepository.findByLanguage(language);
		}
		else {
			categories = categoryRepository.findByLanguageAndHasArticle(language);
		}
		return new CategoryTree(categories);
	}
}
