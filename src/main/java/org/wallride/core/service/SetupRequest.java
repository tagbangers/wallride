package org.wallride.core.service;

import org.hibernate.validator.constraints.Email;
import org.wallride.core.domain.PersonalName;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class SetupRequest implements Serializable {
	
	private String websiteTitle;
	private String defaultLanguage;
	private List<String> languages;
	private String mediaUrlPrefix;
	private String mediaPath;
	private String mailSmtpHost;
	private String mailFrom;
	private String loginId;
	private String loginPassword;
	private PersonalName name;
	private String email;

	public String getWebsiteTitle() {
		return websiteTitle;
	}

	public String getDefaultLanguage() {
		return defaultLanguage;
	}

	public List<String> getLanguages() {
		return languages;
	}

	public String getMediaUrlPrefix() {
		return mediaUrlPrefix;
	}

	public String getMediaPath() {
		return mediaPath;
	}

	public String getMailSmtpHost() {
		return mailSmtpHost;
	}

	public String getMailFrom() {
		return mailFrom;
	}

	public String getLoginId() {
		return loginId;
	}

	public String getLoginPassword() {
		return loginPassword;
	}

	public PersonalName getName() {
		return name;
	}

	public String getEmail() {
		return email;
	}
}
