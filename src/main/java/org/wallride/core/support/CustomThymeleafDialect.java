package org.wallride.core.support;

import org.springframework.stereotype.Component;
import org.thymeleaf.context.IProcessingContext;
import org.thymeleaf.dialect.AbstractDialect;
import org.thymeleaf.dialect.IExpressionEnhancingDialect;

import javax.inject.Inject;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
public class CustomThymeleafDialect extends AbstractDialect implements IExpressionEnhancingDialect {

	@Inject
	private Settings settings;

	@Override
	public Map<String, Object> getAdditionalExpressionObjects(IProcessingContext processingContext) {
		Map<String, Object> objects = new HashMap<>();
		objects.put("posts", new PostUtils(processingContext, settings));
		objects.put("medias", new MediaUtils(settings));
		return Collections.unmodifiableMap(objects);
	}

	@Override
	public String getPrefix() {
		return null;
	}
}
