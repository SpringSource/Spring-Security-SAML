/*
 * Copyright 2002-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.springframework.security.saml2.boot.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.security.saml2.configuration.HostedSaml2IdentityProviderConfiguration;
import org.springframework.security.saml2.configuration.HostedSaml2ServiceProviderConfiguration;
import org.springframework.security.saml2.configuration.HostedSaml2ServerConfiguration;

@ConfigurationProperties(prefix = "spring.security.saml2")
public class SamlBootConfiguration {

	@NestedConfigurationProperty
	private LocalServiceProviderConfiguration serviceProvider;

	@NestedConfigurationProperty
	private LocalIdentityProviderConfiguration identityProvider;

	public LocalServiceProviderConfiguration getServiceProvider() {
		return serviceProvider;
	}

	public void setServiceProvider(LocalServiceProviderConfiguration serviceProvider) {
		this.serviceProvider = serviceProvider;
	}

	public LocalIdentityProviderConfiguration getIdentityProvider() {
		return identityProvider;
	}

	public void setIdentityProvider(LocalIdentityProviderConfiguration identityProvider) {
		this.identityProvider = identityProvider;
	}

	public HostedSaml2ServerConfiguration toSamlServerConfiguration() {
		return new HostedSaml2ServerConfiguration(
			serviceProvider == null ? null : serviceProvider.toHostedConfiguration(),
			identityProvider == null ? null : identityProvider.toHostedConfiguration()
		);
	}

	@Bean
	public HostedSaml2ServiceProviderConfiguration samlServiceProviderConfiguration() {
		return toSamlServerConfiguration().getServiceProvider();
	}

	@Bean
	public HostedSaml2IdentityProviderConfiguration samlIdentityProviderConfiguration() {
		return toSamlServerConfiguration().getIdentityProvider();
	}
}
