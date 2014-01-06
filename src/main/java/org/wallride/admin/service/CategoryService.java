package org.wallride.admin.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;
import org.wallride.admin.support.AuthorizedUser;
import org.wallride.admin.web.category.CategoryCreateForm;
import org.wallride.admin.web.category.CategoryEditForm;
import org.wallride.core.domain.Category;
import org.wallride.core.domain.CategoryTree;
import org.wallride.core.repository.CategoryRepository;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Map;

@Service
@Transactional(rollbackFor=Exception.class)
public class CategoryService {

	public static final String CATEGORY_CACHE_KEY = "categories";

	@Inject
	private CategoryRepository categoryRepository;

	@PersistenceContext
	private EntityManager entityManager;

	@CacheEvict(value=CATEGORY_CACHE_KEY, key="#form.language")
	public Category createCategory(CategoryCreateForm form, Errors errors, AuthorizedUser authorizedUser) {
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

//		int rgt = (parent == null) ? categoryRepository.findMaxRgt() : parent.getRgt();
//		if (rgt == 0) {
//			rgt = 1;
//		}

//		int depth = (parent == null) ? 1 : parent.getDepth() + 1;
//		int sort = categoryRepository.findMaxSortByDepth(depth, form.getLanguage());
//		if (sort == 0 && parent != null) {
//			sort = parent.getSort();
//		}
//		sort++;
//		categoryRepository.incrementSortBySortGreaterThanEqual(sort, form.getLanguage());

		category.setParent(parent);
		category.setCode(form.getCode());
		category.setName(form.getName());
		category.setDescription(form.getDescription());
		category.setLft(rgt);
		category.setRgt(rgt + 1);
//		category.setDepth(depth);
//		category.setSort(sort);
		category.setLanguage(form.getLanguage());

		category = categoryRepository.save(category);

		if (category.getCode() == null) {
			category.setCode(Long.toString(category.getId()));
			category = categoryRepository.save(category);
		}

		return category;
	}

	@CacheEvict(value=CATEGORY_CACHE_KEY, key="#form.language")
	public Category updateCategory(CategoryEditForm form, Errors errors, AuthorizedUser authorizedUser) {
		Category category = categoryRepository.findByIdForUpdate(form.getId(), form.getLanguage());

		categoryRepository.shiftLftRgt(category.getLft(), category.getRgt());
		categoryRepository.shiftRgt(category.getRgt());
		categoryRepository.shiftLft(category.getRgt());

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

//		int depth = (parent == null) ? 1 : parent.getDepth() + 1;
//		int sort = category.getSort();
//		if (depth != category.getDepth()) {
//			sort = categoryRepository.findMaxSortByDepth(depth, form.getLanguage());
//			if (sort == 0 && parent != null) {
//				sort = parent.getSort();
//			}
//			sort++;
//			categoryRepository.incrementSortBySortGreaterThanEqual(sort, form.getLanguage());
//		}

		category.setParent(parent);
		category.setCode(form.getCode());
		category.setName(form.getName());
		category.setDescription(form.getDescription());
		category.setLft(rgt);
		category.setRgt(rgt + 1);
//		category.setDepth(depth);
//		category.setSort(sort);
		category.setLanguage(form.getLanguage());

		category = categoryRepository.save(category);

		if (category.getCode() == null) {
			category.setCode(Long.toString(category.getId()));
			category = categoryRepository.save(category);
		}

		return category;
	}

	@CacheEvict(value=CATEGORY_CACHE_KEY, allEntries=true)
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
//					category.setDepth((int) map.get("depth"));
//					category.setSort(i);
					categoryRepository.save(category);
				}
			}
		}
	}

	@CacheEvict(value=CATEGORY_CACHE_KEY, allEntries=true)
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

//		categoryRepository.decrementSortBySortGreaterThan(category.getSort(), language);
		return category;
	}

	@Cacheable(value=CATEGORY_CACHE_KEY, key="#language")
	public CategoryTree readCategoryTree(String language) {
		List<Category> categories = categoryRepository.findByLanguage(language);
		return new CategoryTree(categories);
	}
}
