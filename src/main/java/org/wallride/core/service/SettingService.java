package org.wallride.core.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.wallride.core.domain.Setting;
import org.wallride.core.repository.SettingRepository;

import javax.inject.Inject;

@Service
@Transactional(rollbackFor=Exception.class)
public class SettingService {
	
	@Inject
	private SettingRepository settingRepository;

	@Cacheable("settings")
	public String readSettingAsString(Setting.Key key) {
		Setting setting = settingRepository.findByKey(key.name());
		return (setting != null) ? setting.getValue() : null;
	}

	@Cacheable("settings")
	public String readSettingAsString(Setting.Key key, String language) {
		Setting setting = settingRepository.findByKey(key.name() + "_" + language);
		return (setting != null) ? setting.getValue() : null;
	}

	@Cacheable("settings")
	public String[] readSettingAsStringArray(Setting.Key key, String delimiter) {
		Setting setting = settingRepository.findByKey(key.name());
		return (setting != null) ? StringUtils.delimitedListToStringArray(setting.getValue(), delimiter) : null;
	}

	@Cacheable("settings")
	public String[] readSettingAsStringArray(Setting.Key key, String delimiter, String language) {
		Setting setting = settingRepository.findByKey(key.name() + "_" + language);
		return (setting != null) ? StringUtils.delimitedListToStringArray(setting.getValue(), delimiter) : null;
	}
}
