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

package org.wallride.web.support;

import org.springframework.mobile.device.Device;
import org.springframework.mobile.device.DeviceResolver;
import org.thymeleaf.context.IProcessingContext;
import org.thymeleaf.context.IWebContext;

public class Devices {

	private IProcessingContext processingContext;

	private DeviceResolver deviceResolver;

	public Devices (IProcessingContext processingContext, DeviceResolver deviceResolver) {
		this.processingContext = processingContext;
		this.deviceResolver = deviceResolver;
	}

	public boolean isMobile() {
		return resolveDevice().isMobile();
	}

	public boolean isNormal() {
		return resolveDevice().isNormal();
	}

	public boolean isTablet() {
		return resolveDevice().isTablet();
	}

	private Device resolveDevice() {
		return deviceResolver.resolveDevice(((IWebContext) processingContext.getContext()).getHttpServletRequest());
	}
}
