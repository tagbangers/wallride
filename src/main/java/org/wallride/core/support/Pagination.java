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

package org.wallride.core.support;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Pagination<T> {

	public static final int DEFAULT_INTERVAL = 5;

	private Page<T> page;
	private String url;

	public Pagination(Page<T> page) {
		this.page = page;
	}

	public Pagination(Page<T> page, String url) {
		this.page = page;
		this.url = url;
	}

	public String getUrl() {
		return url;
	}

	public int getCurrentPageNumber() {
		return page.getNumber();
	}

	public int getPreviousPageNumber() {
		return getCurrentPageNumber() - 1;
	}

	public int getNextPageNumber() {
		return getCurrentPageNumber() + 1;
	}

	public boolean hasPreviousPage() {
		return page.hasPrevious();
	}

	public boolean hasNextPage() {
		return page.hasNext();
	}

	public long getNumberOfFirstElement() {
		long number = 0;
		if (page.hasContent()) {
			number = page.getNumber() * page.getSize() + 1;
		}
		return number;
	}

	public long getNumberOfLastElement() {
		long number = 0;
		if (page.hasContent()) {
			number = getNumberOfFirstElement() + page.getNumberOfElements() - 1;
		}
		return number;
	}

	public long getTotalElements() {
		return page.getTotalElements();
	}

	public int getTotalPages() {
		return page.getTotalPages();
	}

	public List<Pageable> getPageables(Pageable currentPageable) {
		return getPageables(currentPageable, DEFAULT_INTERVAL);
	}

	public List<Pageable> getPageables(Pageable currentPageable, int interval) {
		List<Pageable> pageables = new ArrayList<>();

		int start = page.getNumber() - interval;
		if (start < 0) {
			start = 0;
		}
		int end = page.getNumber() + interval;
		if (end > page.getTotalPages() - 1) {
			end = page.getTotalPages() - 1;
		}

		Pageable p;
		p = currentPageable;
		for (int i = getCurrentPageNumber(); i > start; i--) {
			p = p.previousOrFirst();
			pageables.add(p);
		}

		Collections.reverse(pageables);
		pageables.add(currentPageable);

		p = currentPageable;
		for (int i = getCurrentPageNumber(); i < end; i++) {
			p = p.next();
			pageables.add(p);
		}

		return pageables;
	}
}
