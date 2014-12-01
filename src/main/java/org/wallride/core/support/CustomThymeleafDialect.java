package org.wallride.core.support;

import org.springframework.stereotype.Component;
import org.thymeleaf.context.IProcessingContext;
import org.thymeleaf.dialect.AbstractDialect;
import org.thymeleaf.dialect.IExpressionEnhancingDialect;
import org.wallride.core.service.BlogService;

import javax.inject.Inject;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
public class CustomThymeleafDialect extends AbstractDialect implements IExpressionEnhancingDialect {

	@Inject
	private BlogService blogService;

	@Override
	public Map<String, Object> getAdditionalExpressionObjects(IProcessingContext processingContext) {
		Map<String, Object> objects = new HashMap<>();
		objects.put("posts", new PostUtils(processingContext, blogService));
		objects.put("users", new UserUtils(processingContext, blogService));
		objects.put("medias", new MediaUtils(blogService));
		return Collections.unmodifiableMap(objects);
	}

	@Override
	public String getPrefix() {
		return null;
	}
}
