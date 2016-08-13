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

package org.wallride.web.controller.admin.article;

import org.apache.commons.lang.ArrayUtils;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.servlet.ModelAndView;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.spring4.expression.ThymeleafEvaluationContext;
import org.wallride.domain.*;
import org.wallride.exception.ServiceException;
import org.wallride.service.BlogService;
import org.wallride.service.CustomFieldService;
import org.wallride.service.MediaService;
import org.wallride.support.AuthorizedUser;
import org.wallride.web.support.BlogLanguageMethodArgumentResolver;
import org.wallride.web.support.DefaultModelAttributeInterceptor;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

@Controller
@RequestMapping("/{language}/articles/preview")
public class ArticlePreviewController {

	@Inject
	private BlogService blogService;

	@Inject
	private MediaService mediaService;

	@Inject
	private CustomFieldService customFieldService;

	@Inject
	private ServletContext servletContext;

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
		article.setCover(form.getCoverId() != null ? mediaService.getMedia(form.getCoverId()) : null);
		article.setTitle(form.getTitle());
		article.setBody(form.getBody());
		article.setDate(form.getDate() != null ? form.getDate() : LocalDateTime.now());

		List<CustomFieldValue> fieldValues = new ArrayList<>();
		for (CustomFieldValueEditForm valueForm : form.getCustomFieldValues()) {
			CustomFieldValue value = new CustomFieldValue();
			value.setCustomField(customFieldService.getCustomFieldById(valueForm.getCustomFieldId(), language));
			if (valueForm.getFieldType().equals(CustomField.FieldType.CHECKBOX) && !ArrayUtils.isEmpty(valueForm.getTextValues())) {
				value.setTextValue(String.join(",", valueForm.getTextValues()));
			} else {
				value.setTextValue(valueForm.getTextValue());
			}
			value.setStringValue(valueForm.getStringValue());
			value.setNumberValue(valueForm.getNumberValue());
			value.setDateValue(valueForm.getDateValue());
			value.setDatetimeValue(valueForm.getDatetimeValue());
			fieldValues.add(value);
		}
		article.setCustomFieldValues(new TreeSet<>(fieldValues));
		article.setAuthor(authorizedUser);

		WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(servletContext, "org.springframework.web.servlet.FrameworkServlet.CONTEXT.guestServlet");
		if (context == null) {
			throw new ServiceException("GuestServlet is not ready yet");
		}

		Blog blog = blogService.getBlogById(Blog.DEFAULT_ID);
		BlogLanguage blogLanguage = blog.getLanguage(language);
		request.setAttribute(BlogLanguageMethodArgumentResolver.BLOG_LANGUAGE_ATTRIBUTE, blogLanguage);

		DefaultModelAttributeInterceptor interceptor = context.getBean(DefaultModelAttributeInterceptor.class);
		ModelAndView mv = new ModelAndView("dummy");
		interceptor.postHandle(request, response, this, mv);

		final WebContext ctx = new WebContext(
				request,
				response,
				servletContext,
				LocaleContextHolder.getLocale(),
				mv.getModelMap());
		ctx.setVariable("article", article);

		ThymeleafEvaluationContext evaluationContext = new ThymeleafEvaluationContext(context, null);
		ctx.setVariable(ThymeleafEvaluationContext.THYMELEAF_EVALUATION_CONTEXT_CONTEXT_VARIABLE_NAME, evaluationContext);

		SpringTemplateEngine templateEngine = context.getBean("templateEngine", SpringTemplateEngine.class);
		String html = templateEngine.process("article/describe", ctx);

		response.setContentType("text/html;charset=UTF-8");
		response.setContentLength(html.getBytes("UTF-8").length);
		response.getWriter().write(html);
	}
}
