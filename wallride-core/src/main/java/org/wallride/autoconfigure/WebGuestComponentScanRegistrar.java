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

package org.wallride.autoconfigure;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;
import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.servlet.DispatcherServlet;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class WebGuestComponentScanRegistrar implements ImportBeanDefinitionRegistrar {

	private static final String BEAN_NAME = "webGuestComponentScanBeanPostProcessor";

	@Override
	public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
		Set<String> packagesToScan = getPackagesToScan(importingClassMetadata);
		if (!registry.containsBeanDefinition(BEAN_NAME)) {
			addWebGuestComponentScanBeanPostProcessor(registry, packagesToScan);
		}
		else {
			updateWebGuestComponentScanBeanPostProcessor(registry, packagesToScan);
		}
	}

	private Set<String> getPackagesToScan(AnnotationMetadata metadata) {
		AnnotationAttributes attributes = AnnotationAttributes
				.fromMap(metadata.getAnnotationAttributes(WebGuestComponentScan.class.getName()));
		String[] value = attributes.getStringArray("value");
		String[] basePackages = attributes.getStringArray("basePackages");
		Class<?>[] basePackageClasses = attributes.getClassArray("basePackageClasses");
		if (!ObjectUtils.isEmpty(value)) {
			Assert.state(ObjectUtils.isEmpty(basePackages),
					"@WebGuestComponentScan basePackages and value attributes are mutually exclusive");
		}
		Set<String> packagesToScan = new LinkedHashSet<String>();
		packagesToScan.addAll(Arrays.asList(value));
		packagesToScan.addAll(Arrays.asList(basePackages));
		for (Class<?> basePackageClass : basePackageClasses) {
			packagesToScan.add(ClassUtils.getPackageName(basePackageClass));
		}
		if (packagesToScan.isEmpty()) {
			return Collections.singleton(ClassUtils.getPackageName(metadata.getClassName()));
		}
		return packagesToScan;
	}

	private void addWebGuestComponentScanBeanPostProcessor(BeanDefinitionRegistry registry, Set<String> packagesToScan) {
		GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
		beanDefinition.setBeanClass(WebGuestComponentScanBeanPostProcessor.class);
		beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(toArray(packagesToScan));
		beanDefinition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
		// We don't need this one to be post processed otherwise it can cause a
		// cascade of bean instantiation that we would rather avoid.
		beanDefinition.setSynthetic(true);
		registry.registerBeanDefinition(BEAN_NAME, beanDefinition);
	}

	private void updateWebGuestComponentScanBeanPostProcessor(BeanDefinitionRegistry registry, Set<String> packagesToScan) {
		BeanDefinition definition = registry.getBeanDefinition(BEAN_NAME);
		ConstructorArgumentValues.ValueHolder constructorArguments = definition.getConstructorArgumentValues()
				.getGenericArgumentValue(String[].class);
		Set<String> mergedPackages = new LinkedHashSet<>();
		mergedPackages.addAll(Arrays.asList((String[]) constructorArguments.getValue()));
		mergedPackages.addAll(packagesToScan);
		constructorArguments.setValue(toArray(mergedPackages));
	}

	private String[] toArray(Set<String> set) {
		return set.toArray(new String[set.size()]);
	}

	static class WebGuestComponentScanBeanPostProcessor implements BeanPostProcessor, SmartInitializingSingleton, Ordered {

		private final String[] packagesToScan;

		private boolean processed;

		WebGuestComponentScanBeanPostProcessor(String[] packagesToScan) {
			this.packagesToScan = packagesToScan;
		}

		@Override
		public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
			if (DispatcherServletAutoConfiguration.DEFAULT_DISPATCHER_SERVLET_BEAN_NAME.equals(beanName)) {
				DispatcherServlet dispatcherServlet = (DispatcherServlet) bean;
				AnnotationConfigServletWebServerApplicationContext context = (AnnotationConfigServletWebServerApplicationContext) dispatcherServlet.getWebApplicationContext();
				context.scan(packagesToScan);
				this.processed = true;
			}
			return bean;
		}

		@Override
		public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
			return bean;
		}

		@Override
		public void afterSingletonsInstantiated() {
			Assert.state(this.processed,
					"Unable to configure "
							+ "LocalContainerEntityManagerFactoryBean from @WebGuestComponentScan, "
							+ "ensure an appropriate bean is registered.");
		}

		@Override
		public int getOrder() {
			return 0;
		}
	}
}
