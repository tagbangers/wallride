package org.wallride.web.support;

import org.apache.commons.io.FileUtils;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;
import org.im4java.process.Pipe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.support.WebContentGenerator;
import org.wallride.core.domain.Media;
import org.wallride.core.domain.Setting;
import org.wallride.core.repository.MediaRepository;
import org.wallride.core.support.AmazonS3ResourceUtils;
import org.wallride.core.support.Settings;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class MediaHttpRequestHandler extends WebContentGenerator implements HttpRequestHandler, InitializingBean {

	private MediaRepository mediaRepository;

	private ResourceLoader resourceLoader;

	private Settings settings;

	private static Logger logger = LoggerFactory.getLogger(MediaHttpRequestHandler.class);

	public void setMediaRepository(MediaRepository mediaRepository) {
		this.mediaRepository = mediaRepository;
	}

	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	public void setSettings(Settings settings) {
		this.settings = settings;
	}

	@Override
	public void afterPropertiesSet() throws Exception {

	}

	@Override
	public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		checkAndPrepare(request, response, true);

		Map<String, Object> pathVariables = (Map<String, Object>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
		String key = (String) pathVariables.get("key");

		Media media = mediaRepository.findById(key);
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

		FileCopyUtils.copy(resource.getInputStream(), response.getOutputStream());
	}

//	public Resource readResource(Media media) throws IOException, EncoderException {
//		return readResource(media, 0, 0, null);
//	}

	private Resource readResource(final Media media, final int width, final int height, final Media.ResizeMode mode) throws IOException {
		final Resource prefix = resourceLoader.getResource(settings.readSettingAsString(Setting.Key.MEDIA_PATH));
		final Resource resource = prefix.createRelative(media.getId());
		if (!resource.exists()) {
			return null;
		}

		boolean doResize = (width > 0 || height > 0);
		if (doResize && "image".equals(MediaType.parseMediaType(media.getMimeType()).getType())) {
			final Resource resized = prefix.createRelative(String.format("%s.resized/%dx%d-%d", media.getId(), width, height, mode.ordinal()));
			if (!resized.exists() || resource.lastModified() > resized.lastModified()) {
//			if (!resized.exists()) {
				final File temp = File.createTempFile("resized-", MediaType.parseMediaType(media.getMimeType()).getSubtype());
				temp.deleteOnExit();
				resizeImage(resource, temp, width, height, mode);
				Runnable runnable = new Runnable() {
					@Override
					public void run() {
						try {
							File copied = File.createTempFile("resized-", MediaType.parseMediaType(media.getMimeType()).getSubtype());
							FileUtils.copyFile(temp, copied);
							AmazonS3ResourceUtils.writeFile(temp, resized);
							FileUtils.deleteQuietly(copied);
						}
						catch (Exception e) {
							logger.warn("Image resize failed", e);
						}
					}
				};
				new Thread(runnable).start();
				return new FileSystemResource(temp);
			}
			return resized;
		}
		else {
			return resource;
		}
	}

	private void resizeImage(Resource resource, File file, int width, int height, Media.ResizeMode mode) throws IOException {
		long startTime = System.currentTimeMillis();
		IMOperation op = new IMOperation();
		op.addImage("-");
		switch (mode) {
			case RESIZE:
//				op.resize(width > 0 ? width : null, height > 0 ? height : null, ">");
				op.thumbnail(width > 0 ? width : null, height > 0 ? height : null, ">");
				op.quality(100d);
				break;
			case CROP:
//				op.resize(width > 0 ? width : null, height > 0 ? height : null, "^");
				op.thumbnail(width > 0 ? width : null, height > 0 ? height : null, "^");
				op.quality(100d);
				op.gravity("center");
				op.crop(width, height, 0, 0);
				break;
			default:
				throw new IllegalStateException();
		}
		op.addImage("-");

		try (FileOutputStream out = new FileOutputStream(file); InputStream in = resource.getInputStream();) {
			Pipe pipe = new Pipe(in, out);

			ConvertCmd convert = new ConvertCmd();
//			convert.setSearchPath("C:\\Program Files\\ImageMagick-6.8.6-Q16");
			convert.setInputProvider(pipe);
			convert.setOutputConsumer(pipe);

			convert.run(op);
			out.close();
		}
		catch (InterruptedException e) {
			throw new IOException(e);
		}
		catch (IM4JavaException e) {
			throw new IOException(e);
		}

		long stopTime = System.currentTimeMillis();
		logger.debug("Resized image: time [{}ms]", stopTime - startTime);
//		return file;
	}
}
