package org.wallride.core.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.wallride.core.domain.CustomField;
import org.wallride.core.domain.CustomFieldOption;
import org.wallride.core.model.CustomFieldCreateRequest;
import org.wallride.core.repository.CustomFieldRepository;
import org.wallride.core.support.AuthorizedUser;

import javax.inject.Inject;

@Service
@Transactional(rollbackFor=Exception.class)
public class CustomFieldService {

	@Inject
	private CustomFieldRepository customFieldRepository;

	@CacheEvict(value="articles", allEntries=true)
	public CustomField createCustomField(CustomFieldCreateRequest request, AuthorizedUser authorizedUser) {
		CustomField customField = new CustomField();
		customField.setName(request.getName());
		customField.setDescription(request.getDescription());
		customField.setFieldType(request.getType());
		customField.getCustomFieldOptions().clear();
		if (!CollectionUtils.isEmpty(request.getOptions())) {
			request.getOptions().stream().forEach(optionName -> {
				CustomFieldOption option = new CustomFieldOption();
				option.setName(optionName);
				option.setLanguage(request.getLanguage());
				customField.getCustomFieldOptions().add(option);
			});
		}
		return customFieldRepository.save(customField);
	}

/*	@CacheEvict(value="articles", allEntries=true)
	public CustomField updateCustomField(CustomFieldUpdateRequest request, AuthorizedUser authorizedUser) {
		CustomField customField = customFieldRepository.findOneForUpdateByIdAndLanguage(request.getId(), request.getLanguage());
		CustomField parent = null;
		if (request.getParentId() != null) {
			parent = customFieldRepository.findOneByIdAndLanguage(request.getParentId(), request.getLanguage());
		}

		if (!(customField.getParent() == null && parent == null) && !ObjectUtils.nullSafeEquals(customField.getParent(), parent)) {
			customFieldRepository.shiftLftRgt(customField.getLft(), customField.getRgt());
			customFieldRepository.shiftRgt(customField.getRgt());
			customFieldRepository.shiftLft(customField.getRgt());

			int rgt = 0;
			if (parent == null) {
				rgt = customFieldRepository.findMaxRgt();
				rgt++;
			}
			else {
				rgt = parent.getRgt();
				customFieldRepository.unshiftRgt(rgt);
				customFieldRepository.unshiftLft(rgt);
			}
			customField.setLft(rgt);
			customField.setRgt(rgt + 1);
		}

		customField.setParent(parent);
		customField.setCode(request.getCode() != null ? request.getCode() : request.getName());
		customField.setName(request.getName());
		customField.setDescription(request.getDescription());
		customField.setLanguage(request.getLanguage());

		return customFieldRepository.save(customField);
	}

	@CacheEvict(value="articles", allEntries=true)
	public void updateCustomFieldHierarchy(List<Map<String, Object>> data, String language) {
		for (int i = 0; i < data.size(); i++) {
			Map<String, Object> map = data.get(i);
			if (map.get("item_id") != null) {
				CustomField customField = customFieldRepository.findOneForUpdateByIdAndLanguage(Long.parseLong((String) map.get("item_id")), language);
				if (customField != null) {
					CustomField parent = null;
					if (map.get("parent_id") != null) {
						parent = customFieldRepository.findOneByIdAndLanguage(Long.parseLong((String) map.get("parent_id")), language);
					}
					customField.setParent(parent);
					customField.setLft(((int) map.get("left")) - 1);
					customField.setRgt(((int) map.get("right")) - 1);
					customFieldRepository.save(customField);
				}
			}
		}
	}

	@CacheEvict(value="articles", allEntries=true)
	public CustomField deleteCustomField(long id, String language) {
		CustomField customField = customFieldRepository.findOneForUpdateByIdAndLanguage(id, language);
		CustomField parent = customField.getParent();
		for (CustomField child : customField.getChildren()) {
			child.setParent(parent);
			customFieldRepository.saveAndFlush(child);
		}
		customField.getChildren().clear();
		customFieldRepository.saveAndFlush(customField);
		customFieldRepository.delete(customField);

		customFieldRepository.shiftLftRgt(customField.getLft(), customField.getRgt());
		customFieldRepository.shiftRgt(customField.getRgt());
		customFieldRepository.shiftLft(customField.getRgt());

		return customField;
	}*/

/*
	public CustomField getCustomFieldById(long id, String language) {
		return customFieldRepository.findOneByIdAndLanguage(id, language);
	}

	public CustomField getCustomFieldByCode(String code, String language) {
		return customFieldRepository.findOneByCodeAndLanguage(code, language);
	}

	public Page<CustomField> getCustomFields(CustomFieldSearchRequest request) {
		Pageable pageable = new PageRequest(0, 10);
		return getCustomFields(request, pageable);
	}

	public Page<CustomField> getCustomFields(CustomFieldSearchRequest request, Pageable pageable) {
		return customFieldRepository.search(request, pageable);
	}
*/
}
