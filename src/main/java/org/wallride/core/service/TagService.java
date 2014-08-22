package org.wallride.core.service;

import org.joda.time.LocalDateTime;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.wallride.core.domain.Category;
import org.wallride.core.domain.Tag;
import org.wallride.core.repository.TagRepository;
import org.wallride.core.support.AuthorizedUser;

import javax.annotation.Resource;
import javax.inject.Inject;

@Service
@Transactional(rollbackFor=Exception.class)
public class TagService {

	@Resource
	private TagRepository tagRepository;

	@CacheEvict(value="articles", allEntries=true)
	public Tag createTag(TagCreateRequest request, AuthorizedUser authorizedUser) {
		Tag tag = new Tag();
		LocalDateTime now = LocalDateTime.now();

		tag.setName(request.getName());
		tag.setLanguage(request.getLanguage());

		tag.setCreatedAt(now);
		tag.setCreatedBy(authorizedUser.toString());
		tag.setUpdatedAt(now);
		tag.setUpdatedBy(authorizedUser.toString());
		return tagRepository.saveAndFlush(tag);
	}

	@CacheEvict(value="articles", allEntries=true)
	public Tag updateTag(TagUpdateRequest request, AuthorizedUser authorizedUser) {
		Tag tag = tagRepository.findByIdForUpdate(request.getId(), request.getLanguage());
		LocalDateTime now = LocalDateTime.now();

		tag.setName(request.getName());
		tag.setLanguage(request.getLanguage());

		tag.setUpdatedAt(now);
		tag.setUpdatedBy(authorizedUser.toString());

		return tagRepository.saveAndFlush(tag);
	}

	public Tag readTagById(long id, String language) {
		return tagRepository.findById(id, language);
	}

	public Tag readTagByName(String name, String language) {
		return tagRepository.findByName(name, language);
	}

	public Page<Tag> readTags(TagSearchRequest request) {
		Pageable pageable = new PageRequest(0, 10);
		return readTags(request, pageable);
	}

	public Page<Tag> readTags(TagSearchRequest request, Pageable pageable) {
		return tagRepository.search(request, pageable);
	}
}
