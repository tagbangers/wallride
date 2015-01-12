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

package org.wallride.core.service;

import java.io.Serializable;
import java.util.List;

public class UserBulkDeleteRequest implements Serializable {

	private List<Long> ids;

	public List<Long> getIds() {
		return ids;
	}

	public static class Builder  {

		private List<Long> ids;

		public Builder() {
		}

		public Builder ids(List<Long> ids) {
			this.ids = ids;
			return this;
		}

		public UserBulkDeleteRequest build() {
			UserBulkDeleteRequest request = new UserBulkDeleteRequest();
			request.ids = ids;
			return request;
		}
	}
}
