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

public class UserInvitationCreateRequest implements Serializable {

	private String invitees;
	private String message;

	public String getInvitees() {
		return invitees;
	}

	public String getMessage() {
		return message;
	}

	public static class Builder  {

		private String invitees;
		private String message;

		public Builder() {
		}

		public Builder invitees(String invitees) {
			this.invitees = invitees;
			return this;
		}

		public Builder message(String message) {
			this.message = message;
			return this;
		}

		public UserInvitationCreateRequest build() {
			UserInvitationCreateRequest request = new UserInvitationCreateRequest();
			request.invitees = invitees;
			request.message = message;
			return request;
		}
	}
}
