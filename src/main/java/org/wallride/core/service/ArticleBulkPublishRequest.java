package org.wallride.core.service;

import org.joda.time.LocalDateTime;

import java.io.Serializable;
import java.util.List;

public class ArticleBulkPublishRequest implements Serializable {

	private List<Long> ids;
	private LocalDateTime date;
	private String language;

	public List<Long> getIds() {
		return ids;
	}

	public void setIds(List<Long> ids) {
		this.ids = ids;
	}

	public LocalDateTime getDate() {
		return date;
	}

	public void setDate(LocalDateTime date) {
		this.date = date;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}
}
