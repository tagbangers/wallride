package org.wallride.core.support;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.IProcessingContext;
import org.thymeleaf.dialect.AbstractDialect;
import org.thymeleaf.dialect.IExpressionEnhancingDialect;
import org.wallride.core.service.SettingService;

import javax.inject.Inject;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
public class CustomThymeleafDialect extends AbstractDialect implements IExpressionEnhancingDialect {

	@Inject
	private SettingService settingService;

	@Inject
	private Environment environment;

	@Override
	public Map<String, Object> getAdditionalExpressionObjects(IProcessingContext processingContext) {
		Map<String, Object> objects = new HashMap<>();
		objects.put("posts", new PostUtils(processingContext, settingService, environment));
		objects.put("medias", new MediaUtils(environment));
		return Collections.unmodifiableMap(objects);
	}

	@Override
	public String getPrefix() {
		return null;
	}
}
