package org.wallride.core.service;

import org.joda.time.LocalDateTime;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.wallride.core.domain.Setting;
import org.wallride.core.domain.User;
import org.wallride.core.repository.SettingRepository;
import org.wallride.core.repository.UserRepository;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional(rollbackFor=Exception.class)
public class SetupService {
	
	@Inject
	private SettingRepository settingRepository;
	
	@Inject
	private UserRepository userRepository;

	@CacheEvict(value="settings", allEntries=true)
	public User setup(SetupRequest request) {
		settingRepository.saveAndFlush(new Setting(Setting.Key.DEFAULT_LANGUAGE, request.getDefaultLanguage()));

		Set<String> languages = new LinkedHashSet<>();
		languages.add(request.getDefaultLanguage());
		for (String language : request.getLanguages()) {
			languages.add(language);
		}
		settingRepository.saveAndFlush(new Setting(Setting.Key.LANGUAGES, StringUtils.collectionToCommaDelimitedString(languages)));

		for (String language : languages) {
			settingRepository.saveAndFlush(new Setting(Setting.Key.WEBSITE_TITLE, request.getWebsiteTitle(), language));
		}

		settingRepository.saveAndFlush(new Setting(Setting.Key.MEDIA_URL_PREFIX, request.getMediaUrlPrefix()));
		settingRepository.saveAndFlush(new Setting(Setting.Key.MEDIA_PATH, request.getMediaPath()));

//		settingRepository.saveAndFlush(new Setting(Setting.Key.MAIL_SMTP_HOST, request.getMailSmtpHost()));
//		settingRepository.saveAndFlush(new Setting(Setting.Key.MAIL_FROM, request.getMailFrom()));

		User user = new User();
		user.setLoginId(request.getLoginId());
		
		Md5PasswordEncoder passwordEncoder = new Md5PasswordEncoder();
		user.setLoginPassword(passwordEncoder.encodePassword(request.getLoginPassword(), null));

		user.getName().setFirstName(request.getName().getFirstName());
		user.getName().setLastName(request.getName().getLastName());
		user.setEmail(request.getEmail());

		LocalDateTime now = new LocalDateTime();
		user.setCreatedAt(now);
		user.setUpdatedAt(now);
		
		user = userRepository.saveAndFlush(user);
		return user;
	}
}
