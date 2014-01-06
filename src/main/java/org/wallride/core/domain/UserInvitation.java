package org.wallride.core.domain;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.FieldBridge;
import org.joda.time.LocalDateTime;

import javax.persistence.*;

@Entity
@Table(name="user_invitation")
@DynamicInsert
@DynamicUpdate
@SuppressWarnings("serial")
public class UserInvitation extends DomainObject<String> {

	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name="system-uuid", strategy="uuid2")
	@Column(length=50)
	private String token;

	@Column(length=500, nullable=false)
	private String email;

	@Lob
	private String message;

	@Column(name="expired_at", nullable=false)
	@Type(type="org.jadira.usertype.dateandtime.joda.PersistentLocalDateTime")
	@FieldBridge(impl=LocalDateTimeBridge.class)
	private LocalDateTime expiredAt;

	@Column(nullable=false)
	private boolean accepted;

	@Column(name="accepted_at")
	@Type(type="org.jadira.usertype.dateandtime.joda.PersistentLocalDateTime")
	@FieldBridge(impl=LocalDateTimeBridge.class)
	private LocalDateTime acceptedAt;

	@Override
	public String getId() {
		return getToken();
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public LocalDateTime getExpiredAt() {
		return expiredAt;
	}

	public void setExpiredAt(LocalDateTime expiredAt) {
		this.expiredAt = expiredAt;
	}

	public boolean isAccepted() {
		return accepted;
	}

	public void setAccepted(boolean accepted) {
		this.accepted = accepted;
	}

	public LocalDateTime getAcceptedAt() {
		return acceptedAt;
	}

	public void setAcceptedAt(LocalDateTime acceptedAt) {
		this.acceptedAt = acceptedAt;
	}
}
