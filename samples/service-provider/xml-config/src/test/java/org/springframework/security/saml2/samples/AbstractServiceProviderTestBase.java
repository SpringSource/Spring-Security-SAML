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

package org.springframework.security.saml2.samples;

import java.net.URI;
import java.util.Map;
import java.util.function.Consumer;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.security.saml2.Saml2Transformer;
import org.springframework.security.saml2.boot.configuration.SamlBootConfiguration;
import org.springframework.security.saml2.configuration.HostedSaml2ServiceProviderConfiguration;
import org.springframework.security.saml2.model.Saml2Object;
import org.springframework.security.saml2.model.authentication.Saml2AuthenticationSaml2Request;
import org.springframework.security.saml2.model.metadata.Saml2Metadata;
import org.springframework.security.saml2.model.metadata.Saml2ServiceProviderMetadata;
import org.springframework.security.saml2.serviceprovider.ServiceProviderConfigurationResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.saml2.helper.SamlTestObjectHelper.queryParams;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

abstract class AbstractServiceProviderTestBase {
	@Autowired
	MockMvc mockMvc;

	@Autowired
	Saml2Transformer transformer;

	@Autowired
	SamlBootConfiguration bootConfiguration;

	@SpyBean
	ServiceProviderConfigurationResolver configuration;

	@BeforeEach
	void setUp() {
	}

	@AfterEach
	void reset() {
	}

	Saml2AuthenticationSaml2Request getAuthenticationRequest(String idpEntityId) throws Exception {
		MvcResult result = mockMvc.perform(
			get("/saml/sp/authenticate")
				.param("idp", idpEntityId)
		)
			.andExpect(status().is3xxRedirection())
			.andReturn();

		String location = result.getResponse().getHeader("Location");
		Map<String, String> params = queryParams(new URI(location));
		String request = params.get("SAMLRequest");
		assertNotNull(request);
		String xml = transformer.samlDecode(request, true);
		Saml2Object saml2Object = transformer.fromXml(
			xml,
			bootConfiguration.getServiceProvider().getKeys().toList(),
			bootConfiguration.getServiceProvider().getKeys().toList()
		);
		assertNotNull(saml2Object);
		assertThat(saml2Object.getClass(), equalTo(Saml2AuthenticationSaml2Request.class));
		return (Saml2AuthenticationSaml2Request) saml2Object;
	}

	void mockConfig(Consumer<HostedSaml2ServiceProviderConfiguration.Builder> modifier) {
		Mockito.doAnswer(
			invocation -> {
				HostedSaml2ServiceProviderConfiguration config =
					(HostedSaml2ServiceProviderConfiguration) invocation.callRealMethod();
				HostedSaml2ServiceProviderConfiguration.Builder builder =
					HostedSaml2ServiceProviderConfiguration.builder(config);
				modifier.accept(builder);
				return builder.build();
			}
		)
			.when(configuration).getConfiguration(ArgumentMatchers.any(HttpServletRequest.class));
	}

	Saml2ServiceProviderMetadata getServiceProviderMetadata() throws Exception {
		String xml = mockMvc.perform(get("/saml/sp/metadata"))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();
		assertNotNull(xml);
		Saml2Metadata m = (Saml2Metadata) transformer.fromXml(xml, null, null);
		assertNotNull(m);
		assertThat(m.getClass(), equalTo(Saml2ServiceProviderMetadata.class));
		return (Saml2ServiceProviderMetadata) m;
	}
}
