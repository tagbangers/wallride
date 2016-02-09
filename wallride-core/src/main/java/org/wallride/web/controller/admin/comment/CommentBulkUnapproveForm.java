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

package org.wallride.web.controller.admin.comment;

import org.wallride.core.model.CommentBulkUnapproveRequest;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

public class CommentBulkUnapproveForm implements Serializable {

	private List<Long> ids;
	@NotNull
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

	public CommentBulkUnapproveRequest toCommentBulkUnapproveRequest() {
		CommentBulkUnapproveRequest request = new CommentBulkUnapproveRequest();
		request.setIds(getIds());
		request.setLanguage(getLanguage());
		return request;
	}
}
