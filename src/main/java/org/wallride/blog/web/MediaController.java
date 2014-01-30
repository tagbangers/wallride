package org.wallride.blog.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.ServletWebRequest;
import org.wallride.blog.service.MediaService;
import org.wallride.core.domain.Media;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller @Lazy
@RequestMapping("/{language}/media/{key}")
public class MediaController {

	@Inject
	private MediaService mediaService;

	@Inject
	private ResourceLoader resourceLoader;

	@Inject
	private Environment environment;

	private static Logger logger = LoggerFactory.getLogger(MediaController.class);

	@RequestMapping
	public ResponseEntity<Resource> media(
			@PathVariable String key,
			@RequestParam(value="w", required=false, defaultValue="0") int width,
			@RequestParam(value="h", required=false, defaultValue="0") int height,
			@RequestParam(value="m", required=false, defaultValue="0") int mode,
			HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		Media media = mediaService.readMedia(key);
		Resource resource = mediaService.readResource(media, width, height, Media.ResizeMode.values()[mode]);

		if (resource == null) {
			logger.debug("No matching resource found - returning 404");
			return new ResponseEntity<Resource>(HttpStatus.NOT_FOUND);
		}
		if (new ServletWebRequest(request, response).checkNotModified(resource.lastModified())) {
			logger.debug("Resource not modified - returning 304");
			return new ResponseEntity<Resource>(HttpStatus.NOT_MODIFIED);
		}

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.parseMediaType(media.getMimeType()));
		headers.setContentLength(resource.contentLength());
		headers.setLastModified(resource.lastModified());

		return new ResponseEntity<Resource>(resource, headers, HttpStatus.OK);
	}
}
