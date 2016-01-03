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

package org.wallride.core.job;

import com.google.api.services.analytics.Analytics;
import com.google.api.services.analytics.model.GaData;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.AbstractPagingItemReader;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.wallride.core.domain.Blog;
import org.wallride.core.domain.GoogleAnalytics;
import org.wallride.core.service.BlogService;
import org.wallride.core.exception.GoogleAnalyticsException;
import org.wallride.core.support.GoogleAnalyticsUtils;

import javax.inject.Inject;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
@StepScope
public class UpdatePostViewsItemReader extends AbstractPagingItemReader<List> {

	@Inject
	private BlogService blogService;

	public UpdatePostViewsItemReader() {
		setPageSize(GoogleAnalyticsUtils.MAX_RESULTS);
	}

	@Override
	protected void doReadPage() {
		if (results == null) {
			results = new CopyOnWriteArrayList<>();
		}
		else {
			results.clear();
		}

		Blog blog = blogService.getBlogById(Blog.DEFAULT_ID);
		GoogleAnalytics googleAnalytics = blog.getGoogleAnalytics();
		if (googleAnalytics == null) {
			logger.warn("Configuration of Google Analytics can not be found");
			return;
		}

		Analytics analytics = GoogleAnalyticsUtils.buildClient(googleAnalytics);

		try {
			LocalDate now = LocalDate.now();
			DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			Analytics.Data.Ga.Get request = analytics.data().ga()
					.get(googleAnalytics.getProfileId(), now.minusYears(1).format(dateTimeFormatter), now.format(dateTimeFormatter), "ga:pageViews")
//						.setDimensions(String.format("ga:dimension%d", googleAnalytics.getCustomDimensionIndex()))
//						.setSort(String.format("-ga:dimension%d", googleAnalytics.getCustomDimensionIndex()))
					.setDimensions(String.format("ga:pagePath", googleAnalytics.getCustomDimensionIndex()))
					.setSort(String.format("-ga:pageViews", googleAnalytics.getCustomDimensionIndex()))
					.setStartIndex(getPage() * getPageSize() + 1)
					.setMaxResults(getPageSize());

			logger.info(request.toString());
			final GaData gaData = request.execute();
			if (CollectionUtils.isEmpty(gaData.getRows())) {
				return;
			}

			results.addAll(gaData.getRows());
		} catch (IOException e) {
			logger.warn("Failed to synchronize with Google Analytics", e);
			throw new GoogleAnalyticsException(e);
		}

//		logger.info("Synchronization to google analytics is now COMPLETE. {} posts updated.", count);
	}

	@Override
	protected void doJumpToPage(int i) {
	}
}
