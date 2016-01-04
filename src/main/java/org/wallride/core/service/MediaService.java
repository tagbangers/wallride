/*
 * Copyright 2014 Tagbangers, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wallride.core.service;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.wallride.core.domain.Media;
import org.wallride.core.repository.MediaRepository;
import org.wallride.core.support.ExtendedResourceUtils;
import org.wallride.core.support.WallRideProperties;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;

@Service
@Transactional(rollbackFor=Exception.class)
public class MediaService {

//	@Inject
//	private BlogService blogService;

	@Inject
	private ResourceLoader resourceLoader;

	@Inject
	private WallRideProperties wallRideProperties;

	@javax.annotation.Resource
	private MediaRepository mediaRepository;

	public Media createMedia(MultipartFile file) {
		Media media = new Media();
		media.setMimeType(file.getContentType());
		media.setOriginalName(file.getOriginalFilename());
		media = mediaRepository.saveAndFlush(media);

//		Blog blog = blogService.getBlogById(Blog.DEFAULT_ID);
		try {
			Resource prefix = resourceLoader.getResource(wallRideProperties.getMediaLocation());
			Resource resource = prefix.createRelative(media.getId());
//			AmazonS3ResourceUtils.writeMultipartFile(file, resource);
			ExtendedResourceUtils.write(resource, file);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}

		return media;
	}

	public List<Media> getAllMedias() {
		return mediaRepository.findAll(new Sort(new Sort.Order(Sort.Direction.DESC, "createdAt")));
	}

	public Media getMedia(String id) {
		return mediaRepository.findOneById(id);
	}
}
