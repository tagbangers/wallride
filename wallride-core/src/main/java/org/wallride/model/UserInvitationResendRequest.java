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

package org.wallride.model;

import java.io.Serializable;

public class UserInvitationResendRequest implements Serializable {

	private String token;

	public String getToken() {
		return token;
	}

	public static class Builder  {

		private String token;

		public Builder() {
		}

		public Builder token(String token) {
			this.token = token;
			return this;
		}

		public UserInvitationResendRequest build() {
			UserInvitationResendRequest request = new UserInvitationResendRequest();
			request.token = token;
			return request;
		}
	}
}
