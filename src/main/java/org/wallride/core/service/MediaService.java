package org.wallride.core.service;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.wallride.core.domain.Blog;
import org.wallride.core.domain.Media;
import org.wallride.core.domain.Setting;
import org.wallride.core.repository.MediaRepository;
import org.wallride.core.support.AmazonS3ResourceUtils;
import org.wallride.core.support.Settings;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;

@Service
@Transactional(rollbackFor=Exception.class)
public class MediaService {

	@Inject
	private BlogService blogService;

	@Inject
	private ResourceLoader resourceLoader;

	@javax.annotation.Resource
	private MediaRepository mediaRepository;

	public Media createMedia(MultipartFile file) {
		Media media = new Media();
		media.setMimeType(file.getContentType());
		media.setOriginalName(file.getOriginalFilename());
		media = mediaRepository.saveAndFlush(media);

		Blog blog = blogService.readBlogById(Blog.DEFAULT_ID);
		try {
			Resource prefix = resourceLoader.getResource(blog.getMediaPath());
			Resource resource = prefix.createRelative(media.getId());
			AmazonS3ResourceUtils.writeMultipartFile(file, resource);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}

		return media;
	}

	public List<Media> readAllMedias() {
		return mediaRepository.findAll(new Sort(new Sort.Order(Sort.Direction.DESC, "createdAt")));
	}

	public Media readMedia(String id) {
		return mediaRepository.findById(id);
	}
}
