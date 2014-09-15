package org.wallride.core.domain;

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
