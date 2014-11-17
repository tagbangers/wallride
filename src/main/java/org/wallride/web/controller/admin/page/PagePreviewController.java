package org.wallride.web.controller.admin.page;

import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Qualifier;
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
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.spring4.context.SpringWebContext;
import org.wallride.core.domain.Page;
import org.wallride.core.service.MediaService;
import org.wallride.core.support.AuthorizedUser;
import org.wallride.web.support.DefaultModelAttributeInterceptor;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Controller
@RequestMapping("/{language}/pages/preview")
public class PagePreviewController {

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
			@Valid @ModelAttribute("form") PagePreviewForm form,
			BindingResult result,
			AuthorizedUser authorizedUser,
			Model model,
			HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		Page page = new Page();
		page.setCover(form.getCoverId() != null ? mediaService.readMedia(form.getCoverId()) : null);
		page.setTitle(form.getTitle());
		page.setBody(form.getBody());
		page.setDate(form.getDate() != null ? form.getDate() : new LocalDateTime());
		page.setAuthor(authorizedUser);

		ModelAndView mv = new ModelAndView("dummy");
		defaultModelAttributeInterceptor.postHandle(request, response, this, mv);

		final SpringWebContext ctx = new SpringWebContext(
				request,
				response,
				servletContext,
				LocaleContextHolder.getLocale(),
				mv.getModelMap(),
				WebApplicationContextUtils.getWebApplicationContext(servletContext));
		ctx.setVariable("page", page);

		String html = guestTemplateEngine.process("/page/describe", ctx);

		response.setContentType("text/html;charset=UTF-8");
		response.setContentLength(html.getBytes("UTF-8").length);
		response.getWriter().write(html);
	}
}
