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
