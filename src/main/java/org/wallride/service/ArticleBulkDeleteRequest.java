package org.wallride.service;

import java.io.Serializable;
import java.util.List;

public class ArticleBulkDeleteRequest implements Serializable {

	private List<Long> ids;
	private String language;

	public List<Long> getIds() {
		return ids;
	}

	public void setIds(List<Long> ids) {
		this.ids = ids;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
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

		public ArticleBulkDeleteRequest build() {
			ArticleBulkDeleteRequest request = new ArticleBulkDeleteRequest();
			request.ids = ids;
			request.language = language;
			return request;
		}
	}
}
