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

import com.rometools.rome.feed.atom.Content;
import com.rometools.rome.feed.atom.Entry;
import com.rometools.rome.feed.atom.Feed;
import com.rometools.rome.feed.atom.Link;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.servlet.view.feed.AbstractAtomFeedView;
import org.springframework.web.util.UriComponentsBuilder;
import org.wallride.core.domain.Article;
import org.wallride.core.domain.Blog;
import org.wallride.core.service.BlogService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.ZoneId;
import java.util.*;

public class AtomFeedView extends AbstractAtomFeedView {

	private BlogService blogService;

	public void setBlogService(BlogService blogService) {
		this.blogService = blogService;
	}

	protected void buildFeedMetadata(
			Map<String, Object> model,
			Feed feed,
			HttpServletRequest request) {
		Blog blog = blogService.readBlogById(Blog.DEFAULT_ID);
		String language = LocaleContextHolder.getLocale().getLanguage();

		feed.setTitle(blog.getTitle(language));
		Content info = new Content();
		info.setValue(blog.getTitle(language));
		feed.setInfo(info);

		ArrayList<Link> links = new ArrayList<>();
		Link link = new Link();
		UriComponentsBuilder builder = ServletUriComponentsBuilder.fromCurrentContextPath();
		link.setHref(builder.buildAndExpand().toUriString());
		links.add(link);
		feed.setOtherLinks(links);
//		feed.setIcon("http://" + settings.getAsString(Setting.Key.SITE_URL) + "resources/default/img/favicon.ico");
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected List<Entry> buildFeedEntries(
			Map<String, Object> model,
			HttpServletRequest request,
			HttpServletResponse response)
	throws Exception {
		Set<Article> articles = (Set<Article>)model.get("articles");
		List<Entry> entries = new ArrayList<>(articles.size());
		for (Article article : articles) {
			Entry entry = new Entry();
			entry.setTitle(article.getTitle());
			entry.setPublished(Date.from(article.getDate().atZone(ZoneId.systemDefault()).toInstant()));
			Content content = new Content();
			content.setValue(article.getBody());
			entry.setSummary(content);

			Link link = new Link();
			link.setHref(link(article));
			List<Link> links = new ArrayList<Link>();
			links.add(link);
			entry.setAlternateLinks(links);
			entries.add(entry);
		}
		return entries;
	}

	private String link(Article article) {
		UriComponentsBuilder builder = ServletUriComponentsBuilder.fromCurrentContextPath();
		Map<String, Object> params = new HashMap<>();

		Blog blog = blogService.readBlogById(Blog.DEFAULT_ID);
		if (blog.getLanguages().size() > 1) {
			builder.path("/{language}");
			params.put("language", LocaleContextHolder.getLocale().getLanguage());
		}
		builder.path("/{year}/{month}/{day}/{code}");
		params.put("year", String.format("%04d", article.getDate().getYear()));
		params.put("month", String.format("%02d", article.getDate().getMonth().getValue()));
		params.put("day", String.format("%02d", article.getDate().getDayOfMonth()));
		params.put("code", article.getCode());
		return builder.buildAndExpand(params).encode().toUriString();
	}
}
