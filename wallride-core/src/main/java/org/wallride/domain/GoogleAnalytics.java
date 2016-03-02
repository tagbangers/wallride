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

package org.wallride.domain;

import org.hibernate.search.annotations.Field;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Lob;
import java.io.Serializable;

@Embeddable
public class GoogleAnalytics implements Serializable {

	@Column(name = "ga_tracking_id", length = 100)
	@Field
	private String trackingId;

	@Column(name = "ga_profile_id", length = 100)
	@Field
	private String profileId;

	@Column(name = "ga_custom_dimension_index")
	@Field
	private int customDimensionIndex;

	@Column(name = "ga_service_account_id", length = 300)
	@Field
	private String serviceAccountId;

	@Column(name = "ga_service_account_p12_file_name", length = 300)
	@Field
	private String serviceAccountP12FileName;

	@Lob
	@Column(name = "ga_service_account_p12_file_content")
	private byte[] serviceAccountP12FileContent;

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

	public int getCustomDimensionIndex() {
		return customDimensionIndex;
	}

	public void setCustomDimensionIndex(int customDimensionIndex) {
		this.customDimensionIndex = customDimensionIndex;
	}

	public String getServiceAccountId() {
		return serviceAccountId;
	}

	public void setServiceAccountId(String serviceAccountId) {
		this.serviceAccountId = serviceAccountId;
	}

	public String getServiceAccountP12FileName() {
		return serviceAccountP12FileName;
	}

	public void setServiceAccountP12FileName(String serviceAccountP12FileName) {
		this.serviceAccountP12FileName = serviceAccountP12FileName;
	}

	public byte[] getServiceAccountP12FileContent() {
		return serviceAccountP12FileContent;
	}

	public void setServiceAccountP12FileContent(byte[] serviceAccountP12Key) {
		this.serviceAccountP12FileContent = serviceAccountP12Key;
	}
}
