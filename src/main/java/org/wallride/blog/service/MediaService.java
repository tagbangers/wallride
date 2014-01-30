package org.wallride.blog.service;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.io.FileUtils;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;
import org.im4java.process.Pipe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.wallride.core.domain.Media;
import org.wallride.core.domain.Setting;
import org.wallride.core.repository.MediaRepository;
import org.wallride.core.support.AmazonS3ResourceUtils;
import org.wallride.core.support.Settings;

import javax.inject.Inject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Service @Lazy
@Transactional(rollbackFor=Exception.class)
public class MediaService {

	@Inject
	private MediaRepository mediaRepository;

	@Inject
	private ResourceLoader resourceLoader;

	@Inject
	private Settings settings;

	private static Logger logger = LoggerFactory.getLogger(MediaService.class);

	@Cacheable("medias")
	public Media readMedia(String key) {
		return mediaRepository.findById(key);
	}

//	@Cacheable("resources")
	public Resource readResource(Media media) throws IOException, EncoderException {
		return readResource(media, 0, 0, null);
	}

//	@Cacheable("resources")
	public Resource readResource(final Media media, final int width, final int height, final Media.ResizeMode mode) throws IOException, EncoderException {
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
							copied.delete();
						} catch (Exception e) {
							logger.warn("Image resize failed", e);
						}
					}
				};
				System.out.println("1#############");
				new Thread(runnable).start();
				System.out.println("2#############");
				return new FileSystemResource(temp);
			}
			return resized;
		}
		else {
			return resource;
		}
	}

//	public byte[] resizeImage(Media media, int widtgh, int height, Media.ResizeMode mode) {
//		try {
//			Resource prefix = resourceLoader.getResource(mediaPath);
//			Resource resource = prefix.createRelative(media.getId());
//			return resizeImage(resource, widtgh, height, mode);
//		}
//		catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//	}

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

//		ByteArrayOutputStream out = new ByteArrayOutputStream();
		FileOutputStream out = new FileOutputStream(file);
		Pipe pipe = new Pipe(resource.getInputStream(), out);

		ConvertCmd convert = new ConvertCmd();
//			convert.setSearchPath("C:\\Program Files\\ImageMagick-6.8.6-Q16");
		convert.setInputProvider(pipe);
		convert.setOutputConsumer(pipe);

		try {
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
