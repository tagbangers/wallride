package org.wallride.core.service;

import org.joda.time.LocalDateTime;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.wallride.core.domain.Setting;
import org.wallride.core.domain.User;
import org.wallride.core.repository.SettingRepository;
import org.wallride.core.repository.UserRepository;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(rollbackFor=Exception.class)
public class SetupService {
	
	@Inject
	private SettingRepository settingRepository;
	
	@Inject
	private UserRepository userRepository;

	@CacheEvict(value="settings", allEntries=true)
	public User setup(SetupRequest form, BindingResult result) {
		settingRepository.saveAndFlush(new Setting(Setting.Key.DEFAULT_LANGUAGE, form.getDefaultLanguage()));

		List<String> languages = new ArrayList<>();
		languages.add(form.getDefaultLanguage());
		for (String language : form.getLanguages()) {
			languages.add(language);
		}
		settingRepository.saveAndFlush(new Setting(Setting.Key.LANGUAGES, StringUtils.collectionToCommaDelimitedString(languages)));

		for (String language : languages) {
			settingRepository.saveAndFlush(new Setting(Setting.Key.WEBSITE_TITLE, form.getWebsiteTitle(), language));
		}

		settingRepository.saveAndFlush(new Setting(Setting.Key.MEDIA_URL_PREFIX, form.getMediaUrlPrefix()));
		settingRepository.saveAndFlush(new Setting(Setting.Key.MEDIA_PATH, form.getMediaPath()));

		settingRepository.saveAndFlush(new Setting(Setting.Key.MAIL_SMTP_HOST, form.getMailSmtpHost()));
		settingRepository.saveAndFlush(new Setting(Setting.Key.MAIL_FROM, form.getMailFrom()));

		User user = new User();
		user.setLoginId(form.getLoginId());
		
		Md5PasswordEncoder passwordEncoder = new Md5PasswordEncoder();
		user.setLoginPassword(passwordEncoder.encodePassword(form.getLoginPassword(), null));

		user.getName().setFirstName(form.getName().getFirstName());
		user.getName().setLastName(form.getName().getLastName());
		user.setEmail(form.getEmail());

		LocalDateTime now = new LocalDateTime();
		user.setCreatedAt(now);
		user.setUpdatedAt(now);
		
		user = userRepository.saveAndFlush(user);
		return user;
	}
}
