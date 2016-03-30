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

package org.wallride.web.support;

import com.mortennobel.imagescaling.AdvancedResizeOp;
import com.mortennobel.imagescaling.DimensionConstrain;
import com.mortennobel.imagescaling.ResampleFilters;
import com.mortennobel.imagescaling.ResampleOp;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.support.WebContentGenerator;
import org.wallride.autoconfigure.WallRideProperties;
import org.wallride.domain.Media;
import org.wallride.repository.MediaRepository;
import org.wallride.support.ExtendedResourceUtils;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Map;

public class MediaHttpRequestHandler extends WebContentGenerator implements HttpRequestHandler, InitializingBean {

	private WallRideProperties wallRideProperties;

//	private BlogService blogService;

	private MediaRepository mediaRepository;
	private ResourceLoader resourceLoader;

	private static Logger logger = LoggerFactory.getLogger(MediaHttpRequestHandler.class);

	public void setWallRideProperties(WallRideProperties wallRideProperties) {
		this.wallRideProperties = wallRideProperties;
	}

//	public void setBlogService(BlogService blogService) {
//		this.blogService = blogService;
//	}

	public void setMediaRepository(MediaRepository mediaRepository) {
		this.mediaRepository = mediaRepository;
	}

	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	@Override
	public void afterPropertiesSet() throws Exception {

	}

	@Override
	public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		checkAndPrepare(request, response, true);

		Map<String, Object> pathVariables = (Map<String, Object>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
		String key = (String) pathVariables.get("key");

		Media media = mediaRepository.findOneById(key);
		int width = ServletRequestUtils.getIntParameter(request, "w", 0);
		int height = ServletRequestUtils.getIntParameter(request, "h", 0);
		int mode = ServletRequestUtils.getIntParameter(request, "m", 0);

		Resource resource = readResource(media, width, height, Media.ResizeMode.values()[mode]);

		if (resource == null) {
			logger.debug("No matching resource found - returning 404");
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		if (new ServletWebRequest(request, response).checkNotModified(resource.lastModified())) {
			logger.debug("Resource not modified - returning 304");
			return;
		}

		long length = resource.contentLength();
		if (length > Integer.MAX_VALUE) {
			throw new IOException("Resource content too long (beyond Integer.MAX_VALUE): " + resource);
		}

		response.setContentLength((int) length);
		response.setContentType(media.getMimeType());
		if (!"image".equals(MediaType.parseMediaType(media.getMimeType()).getType())) {
			response.setHeader("Content-Disposition", "attachment;filename*=utf-8''" + URLEncoder.encode(media.getOriginalName(), "UTF-8"));
		}

		FileCopyUtils.copy(resource.getInputStream(), response.getOutputStream());
	}

//	public Resource readResource(Media media) throws IOException, EncoderException {
//		return readResource(media, 0, 0, null);
//	}

	private Resource readResource(final Media media, final int width, final int height, final Media.ResizeMode mode) throws IOException {
//		Blog blog = blogService.getBlogById(Blog.DEFAULT_ID);
//		final Resource prefix = resourceLoader.getResource(blog.getMediaPath());
		final Resource prefix = resourceLoader.getResource(wallRideProperties.getMediaLocation());
		final Resource resource = prefix.createRelative(media.getId());

		if (!resource.exists()) {
			return null;
		}

		Resource resized = resource;
		boolean doResize = (width > 0 || height > 0);
		if (doResize && "image".equals(MediaType.parseMediaType(media.getMimeType()).getType())) {
			resized = prefix.createRelative(String.format("%s.resized/%dx%d-%d",
					media.getId(),
					width, height, mode.ordinal()));
			if (!resized.exists() || resource.lastModified() > resized.lastModified()) {
				File temp = File.createTempFile(
						getClass().getCanonicalName() + ".resized-",
						"." + MediaType.parseMediaType(media.getMimeType()).getSubtype());
				temp.deleteOnExit();
				resizeImage(resource, temp, width, height, mode);

//				AmazonS3ResourceUtils.writeFile(temp, resized);
				ExtendedResourceUtils.write(resized, temp);
				FileUtils.deleteQuietly(temp);
			}
		}
		return resized;
	}

	private void resizeImage(Resource resource, File file, int width, int height, Media.ResizeMode mode) throws IOException {
		long startTime = System.currentTimeMillis();

		if (width <= 0) {
			width = Integer.MAX_VALUE;
		}
		if (height <= 0) {
			height = Integer.MAX_VALUE;
		}

		BufferedImage image = ImageIO.read(resource.getInputStream());

		ResampleOp resampleOp;
		BufferedImage resized;

		switch (mode) {
			case RESIZE:
				resampleOp = new ResampleOp(DimensionConstrain.createMaxDimension(width, height, true));
				resampleOp.setFilter(ResampleFilters.getLanczos3Filter());
				resampleOp.setUnsharpenMask(AdvancedResizeOp.UnsharpenMask.Normal);
				resized = resampleOp.filter(image, null);
				ImageIO.write(resized, StringUtils.getFilenameExtension(file.getName()), file);
				break;
			case CROP:
				float wr = (float) width / (float) image.getWidth();
				float hr = (float) height / (float) image.getHeight();
				float fraction = (wr > hr) ? wr : hr;

				if (fraction < 1) {
					resampleOp = new ResampleOp(DimensionConstrain.createRelativeDimension(fraction));
					resampleOp.setFilter(ResampleFilters.getLanczos3Filter());
					resampleOp.setUnsharpenMask(AdvancedResizeOp.UnsharpenMask.Normal);
					resized = resampleOp.filter(image, null);
				} else {
					resized = image;
				}

				if (resized.getWidth() > width) {
					resized = resized.getSubimage((resized.getWidth() - width) / 2, 0, width, resized.getHeight());
				} else if (resized.getHeight() > height) {
					resized = resized.getSubimage(0, (resized.getHeight() - height) / 2, resized.getWidth(), height);
				}

				ImageIO.write(resized, StringUtils.getFilenameExtension(file.getName()), file);
				break;
			default:
				throw new IllegalStateException();
		}

		long stopTime = System.currentTimeMillis();
		logger.debug("Resized image: time [{}ms]", stopTime - startTime);
	}
}