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
import org.springframework.util.StringUtils;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;
import org.wallride.autoconfigure.WallRideProperties;
import org.wallride.domain.Media;
import org.wallride.service.MediaService;
import org.wallride.support.ExtendedResourceUtils;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Map;

public class MediaHttpRequestHandler extends ResourceHttpRequestHandler implements InitializingBean {

	private static final String MEDIA_ATTRIBUTE = MediaHttpRequestHandler.class.getName() + ".MEDIA";

	private WallRideProperties wallRideProperties;

	private MediaService mediaService;

	private ResourceLoader resourceLoader;

	private static Logger logger = LoggerFactory.getLogger(MediaHttpRequestHandler.class);

	public void setWallRideProperties(WallRideProperties wallRideProperties) {
		this.wallRideProperties = wallRideProperties;
	}

	public void setMediaService(MediaService mediaService) {
		this.mediaService = mediaService;
	}

	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	@Override
	protected Resource getResource(HttpServletRequest request) throws IOException {
		Map<String, Object> pathVariables = (Map<String, Object>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
		String key = (String) pathVariables.get("key");

		Media media = mediaService.getMedia(key);
		if (media == null) {
			return null;
		}

		RequestContextHolder.getRequestAttributes().setAttribute(MEDIA_ATTRIBUTE, media, RequestAttributes.SCOPE_REQUEST);

		int width = ServletRequestUtils.getIntParameter(request, "w", 0);
		int height = ServletRequestUtils.getIntParameter(request, "h", 0);
		Media.ResizeMode mode = Media.ResizeMode.values()[ServletRequestUtils.getIntParameter(request, "m", 0)];

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

	@Override
	protected MediaType getMediaType(HttpServletRequest request, Resource resource) {
		Media media = (Media) RequestContextHolder.getRequestAttributes().getAttribute(MEDIA_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
		return MediaType.parseMediaType(media.getMimeType());
	}

	@Override
	protected void setHeaders(HttpServletResponse response, Resource resource, MediaType mediaType) throws IOException {
		super.setHeaders(response, resource, mediaType);
		if (!"image".equals(mediaType.getType())) {
			Media media = (Media) RequestContextHolder.getRequestAttributes().getAttribute(MEDIA_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
			response.setHeader("Content-Disposition", "attachment;filename*=utf-8''" + URLEncoder.encode(media.getOriginalName(), "UTF-8"));
		}
	}
}