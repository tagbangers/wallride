package org.wallride.core.support;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Pagination<T> {

	public static final int DEFAULT_INTERVAL = 5;

	private Page<T> page;

	public Pagination(Page<T> page) {
		this.page = page;
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
