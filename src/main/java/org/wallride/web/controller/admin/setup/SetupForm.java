package org.wallride.web.controller.admin.setup;

import org.hibernate.validator.constraints.Email;
import org.wallride.core.domain.PersonalName;
import org.wallride.core.service.SetupRequest;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class SetupForm implements Serializable {
	
	@NotNull
	private String websiteTitle;

	@NotNull
	private String defaultLanguage;

	private ArrayList<String> languages = new ArrayList<>();

	@NotNull
	private String mediaUrlPrefix = "/media/";

	@NotNull
	private String mediaPath = "file:" + System.getProperty("java.io.tmpdir") + File.separator + "wallride" + File.separator + "media" + File.separator;

//	@NotNull
//	private String mailSmtpHost;
//
//	@NotNull
//	@Email
//	private String mailFrom;

	@NotNull
	private String loginId;
	
	@NotNull
	private String loginPassword;

	@Valid
	private Name name = new Name();

	@NotNull
	@Email
	private String email;

	public String getWebsiteTitle() {
		return websiteTitle;
	}

	public void setWebsiteTitle(String websiteTitle) {
		this.websiteTitle = websiteTitle;
	}

	public String getDefaultLanguage() {
		return defaultLanguage;
	}

	public void setDefaultLanguage(String defaultLanguage) {
		this.defaultLanguage = defaultLanguage;
	}

	public ArrayList<String> getLanguages() {
		return languages;
	}

	public void setLanguages(ArrayList<String> languages) {
		this.languages = languages;
	}

	public String getMediaUrlPrefix() {
		return mediaUrlPrefix;
	}

	public void setMediaUrlPrefix(String mediaUrlPrefix) {
		this.mediaUrlPrefix = mediaUrlPrefix;
	}

	public String getMediaPath() {
		return mediaPath;
	}

	public void setMediaPath(String mediaPath) {
		this.mediaPath = mediaPath;
	}

//	public String getMailSmtpHost() {
//		return mailSmtpHost;
//	}
//
//	public void setMailSmtpHost(String mailSmtpHost) {
//		this.mailSmtpHost = mailSmtpHost;
//	}
//
//	public String getMailFrom() {
//		return mailFrom;
//	}
//
//	public void setMailFrom(String mailFrom) {
//		this.mailFrom = mailFrom;
//	}

	public String getLoginId() {
		return loginId;
	}

	public void setLoginId(String loginId) {
		this.loginId = loginId;
	}

	public String getLoginPassword() {
		return loginPassword;
	}

	public void setLoginPassword(String loginPassword) {
		this.loginPassword = loginPassword;
	}

	public Name getName() {
		return name;
	}

	public void setName(Name name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public SetupRequest buildSetupRequest() {
		SetupRequest.Builder builder = new SetupRequest.Builder();
		return builder
				.websiteTitle(websiteTitle)
				.defaultLanguage(defaultLanguage)
				.languages(languages)
				.mediaUrlPrefix(mediaUrlPrefix)
				.mediaPath(mediaPath)
//				.mailSmtpHost(mailSmtpHost)
//				.mailFrom(mailFrom)
				.loginId(loginId)
				.loginPassword(loginPassword)
				.name(new PersonalName(name.firstName, name.lastName))
				.email(email)
				.build();
	}

	public static class Name implements Serializable {

		@NotNull
		private String firstName;

		@NotNull
		private String lastName;

		public String getFirstName() {
			return firstName;
		}

		public void setFirstName(String firstName) {
			this.firstName = firstName;
		}

		public String getLastName() {
			return lastName;
		}

		public void setLastName(String lastName) {
			this.lastName = lastName;
		}
	}
}
