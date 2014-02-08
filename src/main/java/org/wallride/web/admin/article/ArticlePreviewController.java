package org.wallride.web.admin.article;

import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.servlet.ModelAndView;
import org.thymeleaf.spring3.SpringTemplateEngine;
import org.thymeleaf.spring3.context.SpringWebContext;
import org.wallride.core.domain.Article;
import org.wallride.core.service.MediaService;
import org.wallride.core.support.AuthorizedUser;
import org.wallride.web.DefaultModelAttributeInterceptor;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Controller @Lazy
@RequestMapping("/{language}/articles/preview")
public class ArticlePreviewController {

	@Inject
	private MediaService mediaService;

	@Inject
	private DefaultModelAttributeInterceptor defaultModelAttributeInterceptor;

	@Inject
	@Qualifier("guestTemplateEngine")
	private SpringTemplateEngine guestTemplateEngine;

	@Inject
	private ServletContext servletContext;

	@Inject
	private Environment environment;

	@RequestMapping
	public void preview(
			@PathVariable String language,
			@Valid @ModelAttribute("form") ArticlePreviewForm form,
			BindingResult result,
			AuthorizedUser authorizedUser,
			Model model,
			HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		Article article = new Article();
		article.setCover(form.getCoverId() != null ? mediaService.readMedia(form.getCoverId()) : null);
		article.setTitle(form.getTitle());
		article.setBody(form.getBody());
		article.setDate(form.getDate() != null ? form.getDate() : new LocalDateTime());
		article.setAuthor(authorizedUser);

		ModelAndView mv = new ModelAndView("dummy");
		defaultModelAttributeInterceptor.postHandle(request, response, this, mv);

		final SpringWebContext ctx = new SpringWebContext(
				request,
				response,
				servletContext,
				LocaleContextHolder.getLocale(),
				mv.getModelMap(),
				WebApplicationContextUtils.getWebApplicationContext(servletContext));
		ctx.setVariable("article", article);

		String html = guestTemplateEngine.process("/article/describe", ctx);

		response.setContentType("text/html;charset=UTF-8");
		response.setContentLength(html.getBytes().length);
		response.getWriter().write(html);
	}
}
