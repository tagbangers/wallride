package org.wallride.core.web;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.wallride.core.domain.Media;

import javax.inject.Inject;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class TemplateUtils {

	@Inject
	private Environment environment;

	public String getMediaUrl(Media media) {
		return getMediaUrl(media.getId());
	}

	public String getMediaUrl(String id) {
		return environment.getRequiredProperty("media.default.url") + id;
	}

	public String replaceMediaUrlForResized(String html) {
		if (!StringUtils.hasText(html)) {
			return null;
		}
		Document document = Jsoup.parse(html);
		Elements elements = document.select("img");
		for (Element element : elements) {
			System.out.println("################################");
			System.out.println(element.attr("src"));

			String src = element.attr("src");
			if (src.startsWith(environment.getRequiredProperty("media.default.url"))) {
				String style = element.attr("style");
				Pattern pattern = Pattern.compile("width: ([0-9]+)px;");
				Matcher matcher = pattern.matcher(element.attr("style"));
				if (matcher.find()) {
					String replaced = src + "?w=" + matcher.group(1);
					element.attr("src", replaced);
				}
				System.out.println(style);
			}
		}
		return document.body().html();
	}
}
