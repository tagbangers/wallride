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

import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;

import java.util.Locale;

public class ExtendedThymeleafViewResolver extends ThymeleafViewResolver {

	@Override
	protected View createView(final String viewName, final Locale locale) throws Exception {
		View view = super.createView(viewName, locale);
		if (view instanceof RedirectView) {
			RedirectView redirectView = (RedirectView) view;
			redirectView.setApplicationContext(getApplicationContext());
			redirectView.setServletContext(getServletContext());
		}
		return view;
	}
}
