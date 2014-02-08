package org.wallride.domain;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="setting")
@DynamicInsert
@DynamicUpdate
@SuppressWarnings("serial")
public class Setting extends DomainObject<String> {

	public enum Key {
		DEFAULT_LANGUAGE,
		LANGUAGES,
		WEBSITE_TITLE,

		MEDIA_URL_PREFIX,
		MEDIA_PATH,

		MAIL_SMTP_HOST,
		MAIL_FROM,
	}

	@Id
	@Column(name="`key`", length=100)
	private String key;

	@Column(length=500, nullable=false)
	private String value;

	public Setting() {}

	public Setting(Key key, String value) {
		this(key, value, null);
	}

	public Setting(Key key, String value, String language) {
		String stringKey = key.name();
		if (language != null) {
			stringKey += "_" + language;
		}
		setKey(stringKey);
		setValue(value);
	}

	@Override
	public String getId() {
		return getKey();
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
