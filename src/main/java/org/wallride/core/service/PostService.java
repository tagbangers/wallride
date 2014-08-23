package org.wallride.core.service;

import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.wallride.core.domain.Post;
import org.wallride.core.repository.PostRepository;

import javax.annotation.Resource;
import javax.inject.Inject;
import java.util.List;

@Service
@Transactional(rollbackFor=Exception.class)
public class PostService {

	@Resource
	private PostRepository postRepository;

	@Inject
	private CacheManager cacheManager;

	private static Logger logger = LoggerFactory.getLogger(PostService.class);

	public List<Post> publishScheduledPosts() {
		logger.info("Starting public posts of the scheduled");

		LocalDateTime now = new LocalDateTime();
		List<Post> posts = postRepository.findByStatusAndDateLessThanEqual(Post.Status.SCHEDULED, now);
		for (Post post : posts) {
			post.setStatus(Post.Status.PUBLISHED);
			postRepository.saveAndFlush(post);
		}

		if (!CollectionUtils.isEmpty(posts)) {
			cacheManager.getCache("articles").clear();
			cacheManager.getCache("pages").clear();
		}

		return posts;
	}

	public Page<Post> readPosts(SearchPostRequest request) {
		Pageable pageable = new PageRequest(0, 10);
		return readPosts(request, pageable);
	}

	public Page<Post> readPosts(SearchPostRequest request, Pageable pageable) {
		return postRepository.search(request, pageable);
	}

	public Post readPostById(long id, String language) {
		return postRepository.findById(id, language);
	}
}
