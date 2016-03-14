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
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.thymeleaf.context.IProcessingContext;
import org.wallride.autoconfigure.WallRideProperties;
import org.wallride.domain.CustomFieldValue;
import org.wallride.domain.Article;
import org.wallride.domain.Blog;
import org.wallride.domain.Page;
import org.wallride.domain.Post;
import org.wallride.support.PageUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Posts {

	private IProcessingContext processingContext;

	private WallRideProperties wallRideProperties;

	private PageUtils pageUtils;

	public Posts(IProcessingContext processingContext, WallRideProperties wallRideProperties, PageUtils pageUtils) {
		this.processingContext = processingContext;
		this.wallRideProperties = wallRideProperties;
		this.pageUtils = pageUtils;
	}

	public String link(Article article) {
		UriComponentsBuilder builder = ServletUriComponentsBuilder.fromCurrentContextPath();
		return path(builder, article, true);
	}

	public String link(Article article, boolean encode) {
		UriComponentsBuilder builder = ServletUriComponentsBuilder.fromCurrentContextPath();
		return path(builder, article, encode);
	}

	public String link(Page page) {
		UriComponentsBuilder builder = ServletUriComponentsBuilder.fromCurrentContextPath();
		return path(builder, page, true);
	}

	public String link(Page page, boolean encode) {
		UriComponentsBuilder builder = ServletUriComponentsBuilder.fromCurrentContextPath();
		return path(builder, page, encode);
	}

	public String path(Article article) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromPath("");
		return path(builder, article, true);
	}

	public String path(Article article, boolean encode) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromPath("");
		return path(builder, article, encode);
	}

	public String path(Page page) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromPath("");
		return path(builder, page, true);
	}

	public String path(Page page, boolean encode) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromPath("");
		return path(builder, page, encode);
	}

	private String path(UriComponentsBuilder builder, Article article, boolean encode) {
		Map<String, Object> params = new HashMap<>();
		builder.path("/{year}/{month}/{day}/{code}");
		params.put("year", String.format("%04d", article.getDate().getYear()));
		params.put("month", String.format("%02d", article.getDate().getMonth().getValue()));
		params.put("day", String.format("%02d", article.getDate().getDayOfMonth()));
		params.put("code", article.getCode());

		UriComponents components = builder.buildAndExpand(params);
		if (encode) {
			components = components.encode();
		}
		return components.toUriString();
	}

	private String path(UriComponentsBuilder builder, Page page, boolean encode) {
		Map<String, Object> params = new HashMap<>();

		List<String> codes = new LinkedList<>();
		Map<Page, String> paths = pageUtils.getPaths(page);
		paths.keySet().stream().map(p -> p.getCode()).forEach(codes::add);

		for (int i = 0; i < codes.size(); i++) {
			String key = "code" + i;
			builder.path("/{" + key + "}");
			params.put(key, codes.get(i));
		}

		UriComponents components = builder.buildAndExpand(params);
		if (encode) {
			components = components.encode();
		}
		return components.toUriString();
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
		return null; //TODO
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

	public String body(Post post) {
		if (!StringUtils.hasText(post.getBody())) {
			return null;
		}

//		Blog blog = blogService.getBlogById(Blog.DEFAULT_ID);
		Document document = Jsoup.parse(post.getBody());
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
		Optional value = target.map(CustomFieldValue::getValue);
		return value.orElse(null);
	}
}
