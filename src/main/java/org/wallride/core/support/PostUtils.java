package org.wallride.core.support;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import org.thymeleaf.context.IProcessingContext;
import org.wallride.core.domain.*;
import org.wallride.core.service.BlogService;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PostUtils {

	private IProcessingContext processingContext;
	private BlogService blogService;

	public PostUtils(IProcessingContext processingContext, BlogService blogService) {
		this.processingContext = processingContext;
		this.blogService = blogService;
	}

	public String link(Article article) {
		UriComponentsBuilder builder = ServletUriComponentsBuilder.fromCurrentContextPath();
		return path(builder, article);
	}

	public String link(Page page) {
		UriComponentsBuilder builder = ServletUriComponentsBuilder.fromCurrentContextPath();
		return path(builder, page);
	}

	public String path(Article article) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromPath("");
		return path(builder, article);
	}

	public String path(Page page) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromPath("");
		return path(builder, page);
	}

	private String path(UriComponentsBuilder builder, Article article) {
		Map<String, Object> params = new HashMap<>();
		String[] languages = (String[]) processingContext.getContext().getVariables().get("LANGUAGES");
		if (languages != null && languages.length > 1) {
			builder.path("/{language}");
			params.put("language", processingContext.getContext().getLocale().getLanguage());
		}
		builder.path("/{year}/{month}/{day}/{code}");
		params.put("year", String.format("%04d", article.getDate().getYear()));
		params.put("month", String.format("%02d", article.getDate().getMonthOfYear()));
		params.put("day", String.format("%02d", article.getDate().getDayOfMonth()));
		params.put("code", article.getCode());
		return builder.buildAndExpand(params).encode().toUriString();
	}

	private String path(UriComponentsBuilder builder, Page page) {
		Map<String, Object> params = new HashMap<>();
		String[] languages = (String[]) processingContext.getContext().getVariables().get("LANGUAGES");
		if (languages != null && languages.length > 1) {
			builder.path("/{language}");
			params.put("language", processingContext.getContext().getLocale().getLanguage());
		}

		PageTree pageTree = (PageTree) processingContext.getContext().getVariables().get("PAGE_TREE_ALL");
//		PageTree pageTree = defaultModelAttributeService.readPageTree(LocaleContextHolder.getLocale().getLanguage());
		List<String> codes = new LinkedList<>();
		Page parent = page.getParent();
		while (parent != null) {
			codes.add(parent.getCode());
			parent = (parent.getParent() != null) ? pageTree.getPageByCode(parent.getParent().getCode()) : null;
		}

		Collections.reverse(codes);
		codes.add(page.getCode());

		for (int i = 0; i < codes.size(); i++) {
			String key = "code" + i;
			builder.path("/{" + key + "}");
			params.put(key, codes.get(i));
		}
		return builder.buildAndExpand(params).encode().toUriString();
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
		Blog blog = blogService.readBlogById(Blog.DEFAULT_ID);
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

	public String ogUrl(Page page, PageTree pageTree) {
		return link(page);
	}

	public String ogImage(Post post) {
		return null; //TODO
	}

	public String title(Post post) {
		if (post.getSeo() != null && post.getSeo().getTitle() != null) {
			return post.getSeo().getTitle();
		}
		Blog blog = blogService.readBlogById(Blog.DEFAULT_ID);
		return String.format("%s | %s",
				post.getTitle(),
				blog.getTitle(processingContext.getContext().getLocale().getLanguage()));
	}

	public String body(Post post) {
		if (!StringUtils.hasText(post.getBody())) {
			return null;
		}

		Blog blog = blogService.readBlogById(Blog.DEFAULT_ID);
		Document document = Jsoup.parse(post.getBody());
		Elements elements = document.select("img");
		for (Element element : elements) {
			String src = element.attr("src");
			if (src.startsWith(blog.getMediaUrlPrefix())) {
				String style = element.attr("style");
				Pattern pattern = Pattern.compile("width: ([0-9]+)px;");
				Matcher matcher = pattern.matcher(element.attr("style"));
				if (matcher.find()) {
					String replaced = src + "?w=" + matcher.group(1);
					element.attr("src", replaced);
				}
			}
		}
		return document.body().html();
	}

	public String summary(Post post, int length) {
		String summary = post.getBody();
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
}
