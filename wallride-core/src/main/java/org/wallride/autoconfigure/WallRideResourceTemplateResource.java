package org.wallride.autoconfigure;

import com.amazonaws.services.s3.model.AmazonS3Exception;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.thymeleaf.spring5.templateresource.SpringResourceTemplateResource;
import org.thymeleaf.templateresource.ITemplateResource;
import org.thymeleaf.util.StringUtils;
import org.thymeleaf.util.Validate;

import java.io.*;

/**
 * @author OGAWA, Takeshi
 */
public class WallRideResourceTemplateResource implements ITemplateResource {

	private final Resource resource;
	private final String characterEncoding;

	public WallRideResourceTemplateResource(final ApplicationContext applicationContext, final String location, final String characterEncoding) {
		super();

		Validate.notNull(applicationContext, "Application Context cannot be null");
		Validate.notEmpty(location, "Resource Location cannot be null or empty");
		// Character encoding CAN be null (system default will be used)

		this.resource = applicationContext.getResource(location);
		this.characterEncoding = characterEncoding;
	}

	public WallRideResourceTemplateResource(final Resource resource, final String characterEncoding) {
		super();

		Validate.notNull(resource, "Resource cannot be null");
		// Character encoding CAN be null (system default will be used)

		this.resource = resource;
		this.characterEncoding = characterEncoding;
	}

	public String getDescription() {
		return this.resource.getDescription();
	}

	public String getBaseName() {
		return computeBaseName(this.resource.getFilename());
	}

	public boolean exists() {
		return this.resource.exists();
	}

	public Reader reader() throws IOException {
		// Will never return null, but an IOException if not found
		try {
			final InputStream inputStream = this.resource.getInputStream();
			if (!StringUtils.isEmptyOrWhitespace(this.characterEncoding)) {
				return new BufferedReader(new InputStreamReader(new BufferedInputStream(inputStream), this.characterEncoding));
			}

			return new BufferedReader(new InputStreamReader(new BufferedInputStream(inputStream)));
		} catch (AmazonS3Exception e) {
			if (e.getStatusCode() == 404) {
				throw new IOException(e);
			}
			throw e;
		}
	}

	public ITemplateResource relative(final String relativeLocation) {
		final Resource relativeResource;
		try {
			relativeResource = this.resource.createRelative(relativeLocation);
		} catch (final IOException e) {
			// Given we have delegated the createRelative(...) mechanism to Spring, it's better if we don't do
			// any assumptions on what this IOException means and simply return a resource object that returns
			// no reader and exists() == false.
			return new WallRideResourceInvalidRelativeTemplateResource(getDescription(), relativeLocation, e);
		}
		return new SpringResourceTemplateResource(relativeResource, this.characterEncoding);
	}


	static String computeBaseName(final String path) {

		if (path == null || path.length() == 0) {
			return null;
		}

		// First remove a trailing '/' if it exists
		final String basePath = (path.charAt(path.length() - 1) == '/' ? path.substring(0, path.length() - 1) : path);

		final int slashPos = basePath.lastIndexOf('/');
		if (slashPos != -1) {
			final int dotPos = basePath.lastIndexOf('.');
			if (dotPos != -1 && dotPos > slashPos + 1) {
				return basePath.substring(slashPos + 1, dotPos);
			}
			return basePath.substring(slashPos + 1);
		} else {
			final int dotPos = basePath.lastIndexOf('.');
			if (dotPos != -1) {
				return basePath.substring(0, dotPos);
			}
		}

		return (basePath.length() > 0 ? basePath : null);

	}

	private static final class WallRideResourceInvalidRelativeTemplateResource implements ITemplateResource {

		private final String originalResourceDescription;
		private final String relativeLocation;
		private final IOException ioException;

		WallRideResourceInvalidRelativeTemplateResource(
				final String originalResourceDescription,
				final String relativeLocation,
				final IOException ioException) {
			super();
			this.originalResourceDescription = originalResourceDescription;
			this.relativeLocation = relativeLocation;
			this.ioException = ioException;
		}

		@Override
		public String getDescription() {
			return "Invalid relative resource for relative location \"" + this.relativeLocation +
					"\" and original resource " + this.originalResourceDescription + ": " + this.ioException.getMessage();
		}

		@Override
		public String getBaseName() {
			return "Invalid relative resource for relative location \"" + this.relativeLocation +
					"\" and original resource " + this.originalResourceDescription + ": " + this.ioException.getMessage();
		}

		@Override
		public boolean exists() {
			return false;
		}

		@Override
		public Reader reader() throws IOException {
			throw new IOException("Invalid relative resource", this.ioException);
		}

		@Override
		public ITemplateResource relative(final String relativeLocation) {
			return this;
		}

		@Override
		public String toString() {
			return getDescription();
		}
	}
}
