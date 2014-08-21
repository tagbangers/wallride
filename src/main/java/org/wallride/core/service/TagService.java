package org.wallride.core.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.wallride.core.domain.Tag;
import org.wallride.core.repository.TagRepository;

import javax.inject.Inject;

@Service
@Transactional(rollbackFor=Exception.class)
public class TagService {

	@Inject
	private TagRepository tagRepository;

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
