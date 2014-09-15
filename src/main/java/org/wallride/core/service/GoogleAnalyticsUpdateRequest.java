package org.wallride.core.service;

import org.springframework.web.multipart.MultipartFile;

public class GoogleAnalyticsUpdateRequest {

	private long blogId;
	private String trackingId;
	private String profileId;
	private Integer customDimensionIndex;
	private String serviceAccountId;
	private MultipartFile serviceAccountP12File;

	public long getBlogId() {
		return blogId;
	}

	public void setBlogId(long blogId) {
		this.blogId = blogId;
	}

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
