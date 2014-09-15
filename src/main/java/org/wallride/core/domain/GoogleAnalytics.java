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

	@Column(name = "ga_custom_dimension_index")
	@Field
	private int customDimensionIndex;

	@Column(name = "ga_service_account_id", length = 300)
	@Field
	private String serviceAccountId;

	@Lob
	@Column(name = "ga_service_account_p12_key")
	private byte[] serviceAccountP12Key;

	public String getTrackingId() {
		return trackingId;
	}

	public void setTrackingId(String trackingId) {
		this.trackingId = trackingId;
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

	public byte[] getServiceAccountP12Key() {
		return serviceAccountP12Key;
	}

	public void setServiceAccountP12Key(byte[] serviceAccountP12Key) {
		this.serviceAccountP12Key = serviceAccountP12Key;
	}
}
