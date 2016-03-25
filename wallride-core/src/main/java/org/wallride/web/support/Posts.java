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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import org.thymeleaf.context.IProcessingContext;
import org.wallride.autoconfigure.WallRideProperties;
import org.wallride.domain.*;
import org.wallride.support.PostUtils;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Posts {

	private IProcessingContext processingContext;

	private PostUtils postUtils;

	private WallRideProperties wallRideProperties;

	public Posts(IProcessingContext processingContext, PostUtils postUtils, WallRideProperties wallRideProperties) {
		this.processingContext = processingContext;
		this.postUtils = postUtils;
		this.wallRideProperties = wallRideProperties;
	}

	public String link(Article article) {
		return postUtils.link(article);
	}

	public String link(Article article, boolean encode) {
		return postUtils.link(article, encode);
	}

	public String link(Page page) {
		return postUtils.link(page);
	}

	public String link(Page page, boolean encode) {
		return postUtils.link(page, encode);
	}

	public String path(Article article) {
		return postUtils.path(article);
	}

	public String path(Article article, boolean encode) {
		return postUtils.path(article, encode);
	}

	public String path(Page page) {
		return postUtils.path(page);
	}

	public String path(Page page, boolean encode) {
		return postUtils.path(page, encode);
	}

	public String metaKeywords(Post post) {
		return post.getSeo() != null ? post.getSeo().getKeywords(): null;
	}

	public String metaAuthor(Post post) {
		return null; //TODO
	}

	public String metaDescription(Post post) {
		return post.getSeo() != null ? post.getSeo().getDescription(): null;
	}

	public String ogSiteName(Post post) {
		Blog blog = (Blog) processingContext.getContext().getVariables().get("BLOG");
		return blog.getTitle(processingContext.getContext().getLocale().getLanguage());
	}

	public String ogTitle(Post post) {
		return post.getTitle();
	}

	public String ogType(Post post) {
		return "article";
	}

	public String ogUrl(Article article) {
		return link(article);
	}

	public String ogUrl(Page page) {
		return link(page);
	}

	public String ogImage(Post post) {
		String path = thumbnail(post);
		if (path == null) {
			return null;
		}
		UriComponentsBuilder builder = ServletUriComponentsBuilder.fromCurrentContextPath();
		builder.path(path);
		return builder.buildAndExpand().encode().toUriString();
	}

	public String title(Post post) {
		if (post.getSeo() != null && post.getSeo().getTitle() != null) {
			return post.getSeo().getTitle();
		}
		Blog blog = (Blog) processingContext.getContext().getVariables().get("BLOG");
		return String.format("%s | %s",
				post.getTitle(),
				blog.getTitle(processingContext.getContext().getLocale().getLanguage()));
	}

	public String thumbnail(Post post) {
		if (post.getCover() != null) {
			return wallRideProperties.getMediaUrlPrefix() + post.getCover().getId();
		}
		else {
			Document document = Jsoup.parse(post.getBody());
			Elements elements = document.select("img");
			for (Element element : elements) {
				return element.attr("src");
			}
		}
		return null;
	}

	public String body(Post post) {
		if (!StringUtils.hasText(post.getBody())) {
			return null;
		}
		return parse(post.getBody());
	}

	public String summary(Post post, int length) {
		Document document = Jsoup.parse(post.getBody());
		String summary = document.text();
		if (!StringUtils.hasText(summary)) {
			return summary;
		}
		summary = summary.replaceAll("<.+?>", "");
		if (!StringUtils.hasText(summary)) {
			return summary;
		}
		if (summary.length() <= length) {
			return summary;
		}
		return summary.substring(0, length) + "...";
	}

	public Object customValue(Post post, String code) {
		Optional<CustomFieldValue> target = post.getCustomFieldValues().stream()
				.filter(v -> v.getCustomField().getCode().equals(code))
				.filter(v -> v.getCustomField().getLanguage().equals(post.getLanguage()))
				.findFirst();
		Object value = target.map(CustomFieldValue::getValue).orElse(null);
		if (value != null && target.get().getCustomField().getFieldType().equals(CustomField.FieldType.HTML)) {
			return parse(target.get().getTextValue());
		}
		return value;
	}

	protected String parse(String html) {
		Document document = Jsoup.parse(html);
		Elements elements = document.select("img");
		for (Element element : elements) {
			String src = element.attr("src");
			if (src.startsWith(wallRideProperties.getMediaUrlPrefix())) {
				String style = element.attr("style");
				Pattern pattern = Pattern.compile("width: ([0-9]+)px;");
				Matcher matcher = pattern.matcher(element.attr("style"));
				if (matcher.find()) {
					String replaced = src + "?w=" + Integer.parseInt(matcher.group(1)) * 2;
					element.attr("src", replaced);
				}
			}
		}
		return document.body().html();
	}
}
