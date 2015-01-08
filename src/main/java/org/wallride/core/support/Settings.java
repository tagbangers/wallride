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

package org.wallride.core.support;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.wallride.core.domain.Setting;
import org.wallride.core.repository.SettingRepository;

import javax.inject.Inject;

@Component
public class Settings {
	
	@Inject
	private SettingRepository settingRepository;

//	@Cacheable("settings")
//	public String readSettingAsString(Setting.Key key) {
//		Setting setting = settingRepository.findByKey(key.name());
//		return (setting != null) ? setting.getValue() : null;
//	}
//
//	@Cacheable("settings")
//	public String readSettingAsString(Setting.Key key, String language) {
//		Setting setting = settingRepository.findByKey(key.name() + "_" + language);
//		return (setting != null) ? setting.getValue() : null;
//	}
//
//	@Cacheable("settings")
//	public String[] readSettingAsStringArray(Setting.Key key, String delimiter) {
//		Setting setting = settingRepository.findByKey(key.name());
//		return (setting != null) ? StringUtils.delimitedListToStringArray(setting.getValue(), delimiter) : null;
//	}
//
//	@Cacheable("settings")
//	public String[] readSettingAsStringArray(Setting.Key key, String delimiter, String language) {
//		Setting setting = settingRepository.findByKey(key.name() + "_" + language);
//		return (setting != null) ? StringUtils.delimitedListToStringArray(setting.getValue(), delimiter) : null;
//	}
}
