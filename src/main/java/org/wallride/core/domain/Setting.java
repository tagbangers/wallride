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

package org.wallride.core.domain;

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
