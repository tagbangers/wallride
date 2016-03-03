package org.wallride.core.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
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
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MessageCodesResolver;
import org.wallride.core.domain.CustomField;
import org.wallride.core.domain.CustomFieldOption;
import org.wallride.core.domain.Tag;
import org.wallride.core.exception.DuplicateCodeException;
import org.wallride.core.exception.EmptyCodeException;
import org.wallride.core.exception.ServiceException;
import org.wallride.core.model.*;
import org.wallride.core.repository.CustomFieldRepository;
import org.wallride.core.support.AuthorizedUser;

import javax.annotation.Resource;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

@Service
@Transactional(rollbackFor=Exception.class)
public class CustomFieldService {

	private static Logger logger = LoggerFactory.getLogger(CustomFieldService.class);

	@Resource
	private CustomFieldRepository customFieldRepository;
	@Inject
	private MessageCodesResolver messageCodesResolver;
	@Inject
	private PlatformTransactionManager transactionManager;

	@CacheEvict(value="customFields", allEntries=true)
	public CustomField createCustomField(CustomFieldCreateRequest request, AuthorizedUser authorizedUser) {
		CustomField customField = new CustomField();
		CustomField duplicate = customFieldRepository.findOneByNameAndLanguage(request.getName(), request.getLanguage());
		if (duplicate != null) {
			throw new DuplicateCodeException(request.getName());
		}
		customField.setIdx(customFieldRepository.countForUpdate(request.getLanguage()) + 1);
		customField.setName(request.getName());
		customField.setDescription(request.getDescription());
		customField.setFieldType(request.getType());
		customField.setLanguage(request.getLanguage());
		customField.getOptions().clear();
		if (!CollectionUtils.isEmpty(request.getOptions())) {
			request.getOptions().stream().forEach(optionName -> {
				CustomFieldOption option = new CustomFieldOption();
				option.setName(optionName);
				option.setLanguage(request.getLanguage());
				customField.getOptions().add(option);
			});
		}
		return customFieldRepository.save(customField);
	}

	@CacheEvict(value="customFields", allEntries=true)
	public CustomField updateCustomField(CustomFieldUpdateRequest request, AuthorizedUser authorizedUser) {
		CustomField customField = customFieldRepository.findOneForUpdateById(request.getId());
		if (customField == null) {
			throw new EmptyCodeException(request.getName());
		}
		customField.setName(request.getName());
		customField.setDescription(request.getDescription());
		customField.setFieldType(request.getType());
		customField.setLanguage(request.getLanguage());
		customField.getOptions().clear();
		if (!CollectionUtils.isEmpty(request.getOptions())) {
			request.getOptions().stream().forEach(optionName -> {
				CustomFieldOption option = new CustomFieldOption();
				option.setName(optionName);
				option.setLanguage(request.getLanguage());
				customField.getOptions().add(option);
			});
		}
		return customFieldRepository.save(customField);
	}


/*
	@CacheEvict(value="customFields", allEntries=true)
	public void updateCustomFieldOrder(List<Map<String, Object>> data, String language) {
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
*/
	@CacheEvict(value="customFields", allEntries=true)
	public CustomField deleteCustomField(CustomFieldDeleteRequest request, BindingResult result) {
		CustomField customField = customFieldRepository.findOneForUpdateByIdAndLanguage(request.getId(), request.getLanguage());
		customFieldRepository.delete(customField);
		return customField;
	}

	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	@CacheEvict(value = "customFields", allEntries = true)
	public List<CustomField> bulkDeleteCustomField(CustomFieldBulkDeleteRequest bulkDeleteRequest, final BindingResult result) {
		List<CustomField> customFields = new ArrayList<>();
		for (long id : bulkDeleteRequest.getIds()) {
			final CustomFieldDeleteRequest deleteRequest = new CustomFieldDeleteRequest.Builder()
					.id(id)
					.language(bulkDeleteRequest.getLanguage())
					.build();

			final BeanPropertyBindingResult r = new BeanPropertyBindingResult(deleteRequest, "request");
			r.setMessageCodesResolver(messageCodesResolver);

			TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
			transactionTemplate.setPropagationBehavior(TransactionTemplate.PROPAGATION_REQUIRES_NEW);
			CustomField customField = null;
			try {
				customField = transactionTemplate.execute(new TransactionCallback<CustomField>() {
					public CustomField doInTransaction(TransactionStatus status) {
						return deleteCustomField(deleteRequest, result);
					}
				});
				customFields.add(customField);
			} catch (Exception e) {
				logger.debug("Errors: {}", r);
				result.addAllErrors(r);
			}
		}
		return customFields;
	}
	
	public CustomField getCustomFieldById(long id, String language) {
		return customFieldRepository.findOneByIdAndLanguage(id, language);
	}

	public CustomField getCustomFieldByName(String name, String language) {
		return customFieldRepository.findOneByNameAndLanguage(name, language);
	}

	public Page<CustomField> getCustomFields(CustomFieldSearchRequest request) {
		Pageable pageable = new PageRequest(0, 10);
		return getCustomFields(request, pageable);
	}

	public Page<CustomField> getCustomFields(CustomFieldSearchRequest request, Pageable pageable) {
		return customFieldRepository.search(request, pageable);
	}

	public SortedSet<CustomField> getAllCustomFields() {
		return new TreeSet<>(customFieldRepository.findAll());
	}
}
