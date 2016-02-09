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

package org.wallride.web.controller.admin.analytics;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.wallride.core.domain.Blog;
import org.wallride.core.domain.GoogleAnalytics;
import org.wallride.core.service.BlogService;

import javax.inject.Inject;

@Controller
@RequestMapping("/{language}/analytics")
public class GoogleAnalyticsDeleteController {

	@Inject
	private BlogService blogService;

	@RequestMapping(method = RequestMethod.DELETE)
	public String delete(
			@PathVariable String language,
			RedirectAttributes redirectAttributes) {
		GoogleAnalytics deletedGoogleAnalytics = blogService.deleteGoogleAnalytics(Blog.DEFAULT_ID);
		redirectAttributes.addFlashAttribute("deletedGoogleAnalytics", deletedGoogleAnalytics);
		return "redirect:/_admin/{language}/analytics";
	}
}
