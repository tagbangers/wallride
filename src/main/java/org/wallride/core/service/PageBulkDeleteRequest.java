package org.wallride.core.service;

import java.io.Serializable;
import java.util.List;

public class PageBulkDeleteRequest implements Serializable {

	private List<Long> ids;
	private String language;

	public List<Long> getIds() {
		return ids;
	}

	public String getLanguage() {
		return language;
	}

	public static class Builder  {

		private List<Long> ids;
		private String language;

		public Builder() {
		}

		public Builder ids(List<Long> ids) {
			this.ids = ids;
			return this;
		}

		public Builder language(String language) {
			this.language = language;
			return this;
		}

		public PageBulkDeleteRequest build() {
			PageBulkDeleteRequest request = new PageBulkDeleteRequest();
			request.ids = ids;
			request.language = language;
			return request;
		}
	}
}
