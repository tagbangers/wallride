package org.wallride.core.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.Errors;
import org.wallride.core.domain.Category;
import org.wallride.core.domain.CategoryTree;
import org.wallride.core.repository.CategoryRepository;
import org.wallride.core.support.AuthorizedUser;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

@Service @Lazy
@Transactional(rollbackFor=Exception.class)
public class CategoryService {

	@Inject
	private CategoryRepository categoryRepository;

	@CacheEvict(value="articles", allEntries=true)
	public Category createCategory(CategoryCreateRequest form, Errors errors, AuthorizedUser authorizedUser) {
		Category category = new Category();

		Category parent = null;
		if (form.getParentId() != null) {
			parent = categoryRepository.findById(form.getParentId(), form.getLanguage());
		}

		int rgt = 0;
		if (parent == null) {
			rgt = categoryRepository.findMaxRgt();
			rgt++;
		}
		else {
			rgt = parent.getRgt();
			categoryRepository.unshiftRgt(rgt);
			categoryRepository.unshiftLft(rgt);
		}

		category.setParent(parent);
		category.setCode(form.getCode() != null ? form.getCode() : form.getName());
		category.setName(form.getName());
		category.setDescription(form.getDescription());
		category.setLft(rgt);
		category.setRgt(rgt + 1);
		category.setLanguage(form.getLanguage());

		return categoryRepository.save(category);
	}

	@CacheEvict(value="articles", allEntries=true)
	public Category updateCategory(CategoryUpdateRequest form, Errors errors, AuthorizedUser authorizedUser) {
		Category category = categoryRepository.findByIdForUpdate(form.getId(), form.getLanguage());
		Category parent = null;
		if (form.getParentId() != null) {
			parent = categoryRepository.findById(form.getParentId(), form.getLanguage());
		}

		if (!(category.getParent() == null && parent == null) && !ObjectUtils.nullSafeEquals(category.getParent(), parent)) {
			categoryRepository.shiftLftRgt(category.getLft(), category.getRgt());
			categoryRepository.shiftRgt(category.getRgt());
			categoryRepository.shiftLft(category.getRgt());

			int rgt = 0;
			if (parent == null) {
				rgt = categoryRepository.findMaxRgt();
				rgt++;
			}
			else {
				rgt = parent.getRgt();
				categoryRepository.unshiftRgt(rgt);
				categoryRepository.unshiftLft(rgt);
			}
			category.setLft(rgt);
			category.setRgt(rgt + 1);
		}

		category.setParent(parent);
		category.setCode(form.getCode() != null ? form.getCode() : form.getName());
		category.setName(form.getName());
		category.setDescription(form.getDescription());
		category.setLanguage(form.getLanguage());

		return categoryRepository.save(category);
	}

	@CacheEvict(value="articles", allEntries=true)
	public void updateCategoryHierarchy(List<Map<String, Object>> data, String language) {
		for (int i = 0; i < data.size(); i++) {
			Map<String, Object> map = data.get(i);
			if (map.get("item_id") != null) {
				Category category = categoryRepository.findByIdForUpdate(Long.parseLong((String) map.get("item_id")), language);
				if (category != null) {
					Category parent = null;
					if (map.get("parent_id") != null) {
						parent = categoryRepository.findById(Long.parseLong((String) map.get("parent_id")), language);
					}
					category.setParent(parent);
					category.setLft(((int) map.get("left")) - 1);
					category.setRgt(((int) map.get("right")) - 1);
					categoryRepository.save(category);
				}
			}
		}
	}

	@CacheEvict(value="articles", allEntries=true)
	public Category deleteCategory(long id, String language) {
		Category category = categoryRepository.findByIdForUpdate(id, language);
		Category parent = category.getParent();
		for (Category child : category.getChildren()) {
			child.setParent(parent);
			categoryRepository.saveAndFlush(child);
		}
		category.getChildren().clear();
		categoryRepository.saveAndFlush(category);
		categoryRepository.delete(category);

		categoryRepository.shiftLftRgt(category.getLft(), category.getRgt());
		categoryRepository.shiftRgt(category.getRgt());
		categoryRepository.shiftLft(category.getRgt());

		return category;
	}

	public CategoryTree readCategoryTree(String language) {
		return readCategoryTree(language, false);
	}

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
