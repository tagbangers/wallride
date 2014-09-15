package org.wallride.core.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.wallride.core.domain.Blog;
import org.wallride.core.repository.BlogRepository;

import javax.annotation.Resource;

@Service
@Transactional(rollbackFor=Exception.class)
public class BlogService {

	@Resource
	private BlogRepository blogRepository;

	@Cacheable(value = "blogs")
	public Blog readBlogById(long id) {
		return blogRepository.findById(id);
	}
}
