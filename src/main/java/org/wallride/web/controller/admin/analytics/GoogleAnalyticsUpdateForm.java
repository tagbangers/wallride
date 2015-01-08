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

package org.wallride.web.controller.admin.analytics;

import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

public class GoogleAnalyticsUpdateForm implements Serializable {

	@NotNull
	private String trackingId;
	@NotNull
	private String profileId;
	@NotNull
	private Integer customDimensionIndex;
	@NotNull
	private String serviceAccountId;
	@NotNull
	private MultipartFile serviceAccountP12File;

	public String getTrackingId() {
		return trackingId;
	}

	public void setTrackingId(String trackingId) {
		this.trackingId = trackingId;
	}

	public String getProfileId() {
		return profileId;
	}

	public void setProfileId(String profileId) {
		this.profileId = profileId;
	}

	public Integer getCustomDimensionIndex() {
		return customDimensionIndex;
	}

	public void setCustomDimensionIndex(Integer customDimensionIndex) {
		this.customDimensionIndex = customDimensionIndex;
	}

	public String getServiceAccountId() {
		return serviceAccountId;
	}

	public void setServiceAccountId(String serviceAccountId) {
		this.serviceAccountId = serviceAccountId;
	}

	public MultipartFile getServiceAccountP12File() {
		return serviceAccountP12File;
	}

	public void setServiceAccountP12File(MultipartFile serviceAccountP12File) {
		this.serviceAccountP12File = serviceAccountP12File;
	}
}
